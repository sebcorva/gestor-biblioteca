package com.duoc.gestor_biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.duoc.gestor_biblioteca.security.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API REST con JWT: sin cookies de sesion, CSRF no aplica
                .csrf(AbstractHttpConfigurer::disable)
                // Cada request se autentica solo con su token, sin HttpSession
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Lectura publica: listado, busqueda, detalle y comentarios
                        .requestMatchers(HttpMethod.GET, "/publicaciones/**").permitAll()
                        // Pagina de error interna de Spring: si no es publica, un error real se ve como
                        // 401
                        .requestMatchers("/error").permitAll()
                        // POST, PUT y DELETE (y cualquier otra ruta) requieren token
                        .anyRequest().authenticated())
                // Sin token o con token invalido en una ruta protegida -> 401 (en vez de
                // redirigir a un login)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
