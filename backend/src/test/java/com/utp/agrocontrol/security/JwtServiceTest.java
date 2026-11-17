package com.utp.agrocontrol.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas unitarias de generacion y validacion de tokens JWT (sin levantar Spring). */
@DisplayName("Pruebas de JwtService")
class JwtServiceTest {

    private static final String SECRET = "X1zvKFUNQLXcc15qilJu4Bg3iiBqA4GFHUjAm+vzPpI=";

    private JwtService jwtService;
    private UserDetails admin;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000);
        admin = User.withUsername("admin").password("x").roles("ADMIN").build();
    }

    @Test
    @DisplayName("Debe generar un token con tres partes (header.payload.signature)")
    void debeGenerarTokenConTresPartes() {
        String token = jwtService.generarToken(admin);
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Debe extraer el username del token generado")
    void debeExtraerUsername() {
        String token = jwtService.generarToken(admin);
        assertThat(jwtService.extraerUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Debe validar un token vigente del mismo usuario")
    void debeValidarTokenVigente() {
        String token = jwtService.generarToken(admin);
        assertThat(jwtService.esTokenValido(token, admin)).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar un token que pertenece a otro usuario")
    void debeRechazarTokenDeOtroUsuario() {
        String token = jwtService.generarToken(admin);
        UserDetails otro = User.withUsername("otro").password("x").roles("USER").build();
        assertThat(jwtService.esTokenValido(token, otro)).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar un token expirado")
    void debeRechazarTokenExpirado() {
        JwtService conExpiracionNegativa = new JwtService(SECRET, -1_000);
        String token = conExpiracionNegativa.generarToken(admin);
        assertThat(jwtService.esTokenValido(token, admin)).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar un token alterado (firma invalida)")
    void debeRechazarTokenAlterado() {
        String token = jwtService.generarToken(admin);
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        assertThat(jwtService.esTokenValido(alterado, admin)).isFalse();
    }

    @Test
    @DisplayName("Debe lanzar JwtException al extraer datos de un token malformado")
    void debeLanzarExcepcionConTokenMalformado() {
        assertThatThrownBy(() -> jwtService.extraerUsername("esto-no-es-un-jwt"))
                .isInstanceOf(JwtException.class);
    }
}
