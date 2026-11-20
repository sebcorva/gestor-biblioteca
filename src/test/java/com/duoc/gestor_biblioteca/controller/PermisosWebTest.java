package com.duoc.gestor_biblioteca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.duoc.gestor_biblioteca.config.SecurityConfig;
import com.duoc.gestor_biblioteca.dto.ComentarioResponse;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;
import com.duoc.gestor_biblioteca.model.Genero;
import com.duoc.gestor_biblioteca.security.JwtService;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;
import com.duoc.gestor_biblioteca.service.ComentarioService;
import com.duoc.gestor_biblioteca.service.PublicacionService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Matriz de permisos del README: SecurityConfig + JwtAuthFilter reales, servicios simulados.
// Los tokens se firman aqui con el secreto de prueba (application-test.properties).
@WebMvcTest(controllers = { PublicacionController.class, ComentarioController.class })
@Import({ SecurityConfig.class, JwtService.class })
@ActiveProfiles("test")
class PermisosWebTest {

    private static final String BODY_PUBLICACION = """
            {"titulo":"Fundación","autor":"Isaac Asimov","anioPublicacionLibro":"1951",
             "genero":"CIENCIA_FICCION",
             "resena":"Un clásico de la ciencia ficción sobre la caída de un imperio galáctico. Imprescindible."}
            """;

    @Autowired
    private MockMvc mvc;

    @Value("${jwt.secret}")
    private String secret;

    @MockitoBean
    private PublicacionService publicacionService;

    @MockitoBean
    private ComentarioService comentarioService;

    @Test
    void lecturaPublicaSinToken() throws Exception {
        when(publicacionService.getAllPublicaciones(any())).thenReturn(List.of());
        when(comentarioService.listarPorPublicacion(1L)).thenReturn(List.of());

        mvc.perform(get("/publicaciones")).andExpect(status().isOk());
        mvc.perform(get("/publicaciones").param("q", "asimov")).andExpect(status().isOk());
        mvc.perform(get("/publicaciones/1/comentarios")).andExpect(status().isOk());
    }

    @Test
    void escriturasSinTokenDan401() throws Exception {
        mvc.perform(json(post("/publicaciones"), BODY_PUBLICACION)).andExpect(status().isUnauthorized());
        mvc.perform(json(put("/publicaciones/1"), BODY_PUBLICACION)).andExpect(status().isUnauthorized());
        mvc.perform(delete("/publicaciones/1")).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/publicaciones/1/comentarios"), "{\"contenido\":\"Hola\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/comentarios/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDa401() throws Exception {
        mvc.perform(json(post("/publicaciones"), BODY_PUBLICACION).header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicarSoloPremiumOAdmin() throws Exception {
        when(publicacionService.createPublicacion(any(), anyLong(), anyString())).thenReturn(respuesta(1L));

        mvc.perform(conToken(json(post("/publicaciones"), BODY_PUBLICACION), token(3L, "USUARIO", false)))
                .andExpect(status().isForbidden());
        mvc.perform(conToken(json(post("/publicaciones"), BODY_PUBLICACION), token(2L, "USUARIO", true)))
                .andExpect(status().isCreated());
        mvc.perform(conToken(json(post("/publicaciones"), BODY_PUBLICACION), token(9L, "ADMIN", false)))
                .andExpect(status().isCreated());
    }

    @Test
    void publicacionInvalidaDa400() throws Exception {
        mvc.perform(conToken(json(post("/publicaciones"), "{\"titulo\":\"\"}"), token(2L, "USUARIO", true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editarAjenoDa403InclusoAlAdmin() throws Exception {
        // El servicio decide la propiedad; aqui se verifica que el 403 llega al cliente.
        when(publicacionService.updatePublicacion(eq(1L), any(), any()))
                .thenAnswer(inv -> {
                    UsuarioAutenticado u = inv.getArgument(2);
                    if (!u.id().equals(2L)) {
                        throw new AccessDeniedException("Solo el autor puede editar esta publicacion");
                    }
                    return respuesta(2L);
                });

        mvc.perform(conToken(json(put("/publicaciones/1"), BODY_PUBLICACION), token(9L, "ADMIN", true)))
                .andExpect(status().isForbidden());
        mvc.perform(conToken(json(put("/publicaciones/1"), BODY_PUBLICACION), token(2L, "USUARIO", true)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarAjenoDa403YAdminDa204() throws Exception {
        doThrow(new AccessDeniedException("no")).when(comentarioService)
                .eliminar(eq(1L), org.mockito.ArgumentMatchers.argThat(u -> u != null && !u.esAdmin()));

        mvc.perform(conToken(delete("/comentarios/1"), token(3L, "USUARIO", false)))
                .andExpect(status().isForbidden());
        mvc.perform(conToken(delete("/comentarios/1"), token(9L, "ADMIN", true)))
                .andExpect(status().isNoContent());
        mvc.perform(conToken(delete("/publicaciones/1"), token(9L, "ADMIN", true)))
                .andExpect(status().isNoContent());
    }

    @Test
    void cualquierUsuarioAutenticadoComenta() throws Exception {
        when(comentarioService.crear(eq(1L), any(), anyLong(), anyString()))
                .thenReturn(new ComentarioResponse(1L, 1L, 3L, "Lector", "Hola", LocalDateTime.now()));

        mvc.perform(conToken(json(post("/publicaciones/1/comentarios"), "{\"contenido\":\"Hola\"}"),
                token(3L, "USUARIO", false)))
                .andExpect(status().isCreated());
    }

    private String token(Long id, String rol, boolean premium) {
        SecretKey clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("email", "u" + id + "@biblioteca.cl")
                .claim("nombre", "Usuario " + id)
                .claim("rol", rol)
                .claim("premium", premium)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(600)))
                .signWith(clave)
                .compact();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private static PublicacionResponse respuesta(Long usuarioId) {
        return new PublicacionResponse(1L, "Fundación", "Isaac Asimov", "1951", Genero.CIENCIA_FICCION,
                "Reseña", LocalDateTime.now(), usuarioId, "Usuario " + usuarioId);
    }
}
