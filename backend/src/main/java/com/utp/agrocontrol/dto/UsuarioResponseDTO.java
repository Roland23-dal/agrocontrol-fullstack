package com.utp.agrocontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Vista publica de un usuario (nunca incluye la contrasena). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private boolean enabled;
    private List<String> roles;
}
