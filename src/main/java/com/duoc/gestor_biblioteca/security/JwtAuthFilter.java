package com.duoc.gestor_biblioteca.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String AUTHORITY_PREMIUM = "PREMIUM";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Sin token
        if (header == null || !header.startsWith(PREFIJO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIJO_BEARER.length());

        try {
            Claims claims = jwtService.validarToken(token);

            UsuarioAutenticado usuario = new UsuarioAutenticado(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("nombre", String.class),
                    claims.get("rol", String.class),
                    Boolean.TRUE.equals(claims.get("premium", Boolean.class)));

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.rol()));
            if (usuario.premium()) {
                authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREMIUM));
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(usuario, null,
                    authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT rechazado: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

}
