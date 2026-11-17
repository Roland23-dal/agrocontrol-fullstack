package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.dto.AuthResponseDTO;
import com.utp.agrocontrol.dto.LoginRequestDTO;
import com.utp.agrocontrol.dto.RegisterRequestDTO;
import com.utp.agrocontrol.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints publicos de autenticacion (no requieren token).
 *  - POST /api/auth/register : crea un usuario con rol USER y devuelve su token JWT
 *  - POST /api/auth/login    : valida credenciales y devuelve el token JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registrar(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
