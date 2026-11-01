package com.duoc.gestor_biblioteca.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey clave;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Verifica la firma y expiracion
    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
