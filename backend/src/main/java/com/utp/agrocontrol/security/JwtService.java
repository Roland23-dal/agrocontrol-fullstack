package com.utp.agrocontrol.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Genera y valida tokens JWT (JSON Web Token) firmados con HMAC-SHA.
 *
 * Estructura del token: header.payload.signature
 *  - subject (sub): nombre de usuario
 *  - roles: lista de autoridades (ej. ROLE_ADMIN)
 *  - iat / exp: fecha de emision y de expiracion
 * La clave secreta (Base64) y el tiempo de vida se configuran en application.properties.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secretBase64,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationMs = expirationMs;
    }

    public String generarToken(UserDetails usuario) {
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Devuelve el username (subject) del token. Lanza JwtException si el token es invalido o expiro. */
    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    /** true si la firma es correcta, no expiro y pertenece al usuario indicado. */
    public boolean esTokenValido(String token, UserDetails usuario) {
        try {
            Claims claims = extraerClaims(token);
            return usuario.getUsername().equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
