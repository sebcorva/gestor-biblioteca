# Basado en el backend de ejemplo del profesor (cvalverd), adaptado a Java 17 y a gestor-biblioteca.

# ---- Etapa 1: compilar con Maven Wrapper ----
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# mvnw puede venir con saltos de linea de Windows (CRLF) si se edito en Windows
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B clean package -DskipTests

# ---- Etapa 2: imagen liviana solo con el JRE ----
FROM eclipse-temurin:17-jre

WORKDIR /app
RUN useradd --system --no-create-home appuser \
    && mkdir -p /app/wallet \
    && chown appuser:appuser /app/wallet \
    && chmod 0700 /app/wallet
COPY --from=build /app/target/gestor-biblioteca-0.0.1-SNAPSHOT.jar /app/gestor-biblioteca.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# La wallet llega por S3 (AWS) o montada en /app/wallet (docker compose). Nunca dentro de la imagen.
ENV TNS_ADMIN=/app/wallet
RUN chmod 0555 /app/docker-entrypoint.sh
USER appuser
EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
