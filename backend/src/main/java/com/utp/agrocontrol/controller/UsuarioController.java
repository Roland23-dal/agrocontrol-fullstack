package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.dto.AsignarRolesDTO;
import com.utp.agrocontrol.dto.UsuarioResponseDTO;
import com.utp.agrocontrol.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion de usuarios. Base URL: /api/usuarios
 * Aqui se usa @Secured (seguridad por rol a nivel de metodo) para las operaciones
 * de administracion, y @PreAuthorize("isAuthenticated()") para consultar el perfil propio.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET /api/usuarios/me -> datos del usuario autenticado (cualquier rol)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> perfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(authentication.getName()));
    }

    @Secured("ROLE_ADMIN")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/roles")
    public ResponseEntity<UsuarioResponseDTO> asignarRoles(@PathVariable Long id,
                                                           @Valid @RequestBody AsignarRolesDTO dto) {
        return ResponseEntity.ok(usuarioService.asignarRoles(id, dto.getRoles()));
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
