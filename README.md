# gestor-biblioteca (foro)

Microservicio de **foro de recomendaciones de libros**: búsqueda, visualización, publicación de reseñas y comentarios.

Forma parte de un sistema de dos microservicios independientes, cada uno con su propio repositorio y su propio esquema Oracle:

| Microservicio | Puerto | Esquema Oracle | Responsabilidad |
|---|---|---|---|
| auth-users | 8081 | `USUARIOS_APP` | Usuarios, roles, login (emite el JWT) y pago premium |
| **gestor-biblioteca** (este repo) | 8080 | `FORO_APP` | Foro: publicaciones de libros y comentarios (valida el JWT) |

El foro **no llama** a auth-users: valida la firma del JWT con el mismo secreto (`jwt.secret`) y toma del token el id, nombre, rol y estado premium del usuario.

```
Postman ── POST /auth/login ──► auth-users (8081) ── emite el JWT
   │
   └── Authorization: Bearer <JWT> ──► foro (8080) ── verifica la firma con el mismo secreto
```

## Tecnologías

- Java 17 y Spring Boot 4.1.2 (Web, Data JPA, Validation, Security)
- JJWT 0.12.6 (solo validación de tokens HS256)
- Oracle Autonomous Database (conexión con wallet)
- Lombok

## Cómo ejecutar

La wallet de Oracle (`src/main/resources/wallet/`) y la configuración (`application.properties`) vienen incluidas en el repositorio, así que no hace falta configurar nada.

1. Levantar **auth-users** primero (para poder hacer login y obtener tokens):
   ```bash
   cd auth-users
   ./mvnw spring-boot:run
   ```
2. Levantar el foro:
   ```bash
   cd gestor-biblioteca
   ./mvnw spring-boot:run
   ```

La API queda disponible en `http://localhost:8080`. Las tablas se crean solas al arrancar (`ddl-auto=update`).

## Usuarios de prueba

Están cargados en la base de datos de **auth-users** (ver su README). El login se hace en `POST http://localhost:8081/auth/login`.

| Email | Contraseña | Rol | Premium | En el foro puede |
|---|---|---|---|---|
| `admin@biblioteca.cl` | `Admin2026!` | ADMIN | sí | Publicar, comentar y eliminar cualquier publicación o comentario |
| `premium@biblioteca.cl` | `Premium2026!` | USUARIO | sí | Publicar y comentar; editar y eliminar lo propio |
| `usuario@biblioteca.cl` | `Usuario2026!` | USUARIO | no | Solo comentar (al publicar recibe `403`) |

## Roles y seguridad

- **Lectura pública**: cualquiera (sin token) puede buscar y ver publicaciones y comentarios.
- **Publicar**: solo usuarios **premium** o **ADMIN** (`@PreAuthorize("hasRole('ADMIN') or hasAuthority('PREMIUM')")`).
- **Comentar**: cualquier usuario autenticado.
- **Editar una publicación**: solo su autor (ni siquiera el ADMIN).
- **Eliminar una publicación o un comentario**: su autor o un ADMIN (moderación).
- Los comentarios **no se editan**.

El autor de una publicación o comentario (`usuarioId`, `usuarioNombre`) se toma **del token**, nunca del body. Así nadie puede publicar a nombre de otro.

| Acción | Sin token | USUARIO | USUARIO premium | ADMIN |
|---|:-:|:-:|:-:|:-:|
| Buscar / ver publicaciones y comentarios | ✅ | ✅ | ✅ | ✅ |
| Crear una publicación | ❌ 401 | ❌ 403 | ✅ | ✅ |
| Editar una publicación | ❌ 401 | solo propia | solo propia | solo propia |
| Eliminar una publicación | ❌ 401 | solo propia | solo propia | ✅ cualquiera |
| Comentar | ❌ 401 | ✅ | ✅ | ✅ |
| Eliminar un comentario | ❌ 401 | solo propio | solo propio | ✅ cualquiera |

Las rutas protegidas exigen el header:

```
Authorization: Bearer <token>
```

El token se valida en cada request (`JwtAuthFilter`), sin sesión en el servidor (`STATELESS`). Del token se obtienen las authorities `ROLE_ADMIN` / `ROLE_USUARIO` y, si el usuario es premium, `PREMIUM`.

## Endpoints

### Publicaciones

| Método | Ruta | Quién puede | Respuesta |
|---|---|---|---|
| GET | `/publicaciones` | Público | `200` todas, de la más nueva a la más antigua |
| GET | `/publicaciones?q=texto` | Público | `200` las que contienen el texto en el **título o el autor** (sin distinguir mayúsculas) |
| GET | `/publicaciones/{id}` | Público | `200` detalle (sin comentarios) · `404` |
| POST | `/publicaciones` | Premium o ADMIN | `201` · `403` si no es premium |
| PUT | `/publicaciones/{id}` | Solo el autor | `200` · `403` · `404` |
| DELETE | `/publicaciones/{id}` | El autor o ADMIN | `204` (elimina también sus comentarios) · `403` · `404` |

Body de `POST` y `PUT`:

| Campo | Regla |
|---|---|
| `titulo` | Obligatorio, máx. 100 caracteres |
| `autor` | Obligatorio, máx. 100 caracteres (autor del libro) |
| `anioPublicacionLibro` | Obligatorio, texto de 1 a 4 dígitos (ej. `"1967"`) |
| `genero` | Obligatorio: `FICCION`, `FANTASIA`, `CIENCIA_FICCION`, `MISTERIO`, `THRILLER`, `ROMANCE`, `HISTORICO`, `BIOGRAFIA`, `AUTOAYUDA`, `TECNOLOGIA`, `EDUCACION`, `FILOSOFIA`, `POESIA` o `TERROR` |
| `resena` | Obligatoria, entre 50 y 1000 caracteres |

La fecha de publicación la asigna el servidor y no cambia al editar.

### Comentarios

| Método | Ruta | Quién puede | Respuesta |
|---|---|---|---|
| GET | `/publicaciones/{id}/comentarios` | Público | `200` comentarios de la publicación, del más antiguo al más nuevo · `404` si la publicación no existe |
| POST | `/publicaciones/{id}/comentarios` | Usuario autenticado | `201` · `404` |
| DELETE | `/comentarios/{id}` | El autor o ADMIN | `204` · `403` · `404` |

Body de `POST`: `contenido` (obligatorio, máx. 500 caracteres).

## Errores

Todas las respuestas de error (salvo el `401`, que va sin cuerpo) tienen el mismo formato:

```json
{ "status": 404, "mensaje": "No existe la publicacion con id 99", "detalles": [], "timestamp": "2026-11-01T18:00:00" }
```

| Código | Cuándo |
|---|---|
| `400` | Validación fallida (`detalles` indica el campo), JSON mal formado, género inexistente o parámetro con tipo inválido |
| `401` | Sin token, o token inválido o expirado, en una ruta protegida |
| `403` | Token válido, pero sin permisos (no premium al publicar, o no es el autor) |
| `404` | Publicación, comentario o ruta inexistente |
| `405` | Método HTTP no permitido |
| `500` | Error inesperado (sin detalles internos) |

## Prueba con Postman

### Opción A: importar la colección

En Postman: **Import** → [`postman/gestor-biblioteca.postman_collection.json`](postman/gestor-biblioteca.postman_collection.json).

- No necesita environment: las URLs (`baseUsuarios`, `baseForo`), los tokens y los ids se guardan como **variables de la colección**.
- Las carpetas están numeradas y conviene ejecutarlas en orden (o con **Run collection**): la carpeta `0. Login` obtiene los 3 tokens y los siguientes requests los usan solos.
- Cada request tiene un test con el código esperado (`201`, `403`, etc.).

### Opción B: paso a paso, a mano

#### 1. Login con los 3 usuarios

`POST http://localhost:8081/auth/login` → copiar el `token` de la respuesta. En los requests del foro se pega en **Authorization → Bearer Token**.

```json
{ "email": "admin@biblioteca.cl", "password": "Admin2026!" }
```
```json
{ "email": "premium@biblioteca.cl", "password": "Premium2026!" }
```
```json
{ "email": "usuario@biblioteca.cl", "password": "Usuario2026!" }
```

#### 2. Crear 3 publicaciones

`POST http://localhost:8080/publicaciones`. Las publicaciones 1 y 2 con el token de **premium** y la 3 con el de **admin**. Anotar el `idPublicacion` de cada respuesta.

Publicación 1 (premium):
```json
{
  "titulo": "Cien años de soledad",
  "autor": "Gabriel García Márquez",
  "anioPublicacionLibro": "1967",
  "genero": "FICCION",
  "resena": "Una saga familiar increíble que mezcla lo cotidiano con lo mágico. La historia de los Buendía en Macondo te atrapa de principio a fin."
}
```

Publicación 2 (premium):
```json
{
  "titulo": "Fundación",
  "autor": "Isaac Asimov",
  "anioPublicacionLibro": "1951",
  "genero": "CIENCIA_FICCION",
  "resena": "Un clásico de la ciencia ficción sobre la caída de un imperio galáctico y el plan para acortar la era de oscuridad. Imprescindible."
}
```

Publicación 3 (admin):
```json
{
  "titulo": "El túnel",
  "autor": "Ernesto Sabato",
  "anioPublicacionLibro": "1948",
  "genero": "MISTERIO",
  "resena": "Una novela breve e intensa narrada por un pintor obsesionado. Explora los celos y la soledad con una prosa que no da respiro."
}
```

#### 3. Comentar (1 por publicación)

`POST http://localhost:8080/publicaciones/{idPublicacion}/comentarios`

En la publicación 1, con el token de **usuario**:
```json
{ "contenido": "Lo leí en el colegio y me encantó. Macondo es inolvidable." }
```

En la publicación 2, con el token de **admin**:
```json
{ "contenido": "Gran recomendación, la saga completa vale mucho la pena." }
```

En la publicación 3, con el token de **premium**:
```json
{ "contenido": "Corto pero muy intenso, se lee en una tarde." }
```

#### 4. Verificar permisos

| # | Token | Request | Esperado |
|---|---|---|---|
| 1 | (ninguno) | `GET /publicaciones` | `200` las 3, la más nueva primero |
| 2 | (ninguno) | `GET /publicaciones?q=asimov` | `200` solo Fundación |
| 3 | (ninguno) | `GET /publicaciones/{id 1}/comentarios` | `200` con el comentario de Ursula |
| 4 | (ninguno) | `POST /publicaciones` (cualquier body válido) | `401` |
| 5 | usuario | `POST /publicaciones` (cualquier body válido) | `403` (no es premium) |
| 6 | usuario | `PUT /publicaciones/{id 1}` | `403` (no es el autor) |
| 7 | usuario | `DELETE /publicaciones/{id 1}` | `403` |
| 8 | admin | `PUT /publicaciones/{id 1}` | `403` (editar es solo del autor) |
| 9 | premium | `PUT /publicaciones/{id 1}` cambiando la reseña | `200`, misma `fechaPublicacion` |
| 10 | premium | `DELETE /comentarios/{comentario de la publicación 2}` | `403` (es del admin) |
| 11 | admin | `DELETE /comentarios/{comentario de la publicación 1}` | `204` (moderación) |
| 12 | admin | `DELETE /publicaciones/{id 2}` | `204`, y `GET /publicaciones/{id 2}/comentarios` → `404` |

**Extra (premium por pago):** con el token de `usuario`, `POST http://localhost:8081/pagos/premium` devuelve en `sesion.token` un token nuevo con `premium: true`. Con ese token, el caso 5 pasa a `201`.

## Base de datos

[`script_bd.sql`](script_bd.sql) contiene el **DDL** de las tablas `publicaciones` y `comentarios` (FK `comentarios.publicacion_id → publicaciones.id_publicacion`), para ejecutar conectado como `FORO_APP`. Hibernate las crea con `ddl-auto=update`, así que el DDL queda como referencia.

No incluye datos: las publicaciones y comentarios se crean con la API (paso a paso arriba), porque el autor sale del JWT. No hay FK hacia `usuarios`: ese dato vive en el otro microservicio y el foro guarda `usuario_id` y `usuario_nombre` como copia.

## Estructura

```
src/main/java/com/duoc/gestor_biblioteca/
├── config/        SecurityConfig
├── controller/    PublicacionController, ComentarioController
├── dto/           Records de request y response (con static from())
├── exception/     Excepciones propias y GlobalExceptionHandler
├── model/         Publicacion, Comentario, Genero
├── repository/    PublicacionRepository (búsqueda con @Query), ComentarioRepository
├── security/      JwtService (solo valida), JwtAuthFilter, UsuarioAutenticado
└── service/       Interfaces e implementaciones (Publicacion, Comentario)
```
