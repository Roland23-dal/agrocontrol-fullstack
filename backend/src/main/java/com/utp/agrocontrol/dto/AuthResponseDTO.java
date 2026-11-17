package com.utp.agrocontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Respuesta de login / registro: contiene el token JWT que el cliente debe enviar en cada solicitud. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String tipo = "Bearer";
    private String username;
    private List<String> roles;
    private long expiraEnMs;

    public AuthResponseDTO(String token, String username, List<String> roles, long expiraEnMs) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expiraEnMs = expiraEnMs;
    }
}
