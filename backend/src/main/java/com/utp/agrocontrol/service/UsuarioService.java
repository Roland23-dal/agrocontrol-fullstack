package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.UsuarioResponseDTO;
import com.utp.agrocontrol.model.NombreRol;

import java.util.List;
import java.util.Set;

public interface UsuarioService {
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO buscarPorId(Long id);
    UsuarioResponseDTO obtenerPerfil(String username);
    UsuarioResponseDTO asignarRoles(Long id, Set<NombreRol> roles);
    void eliminar(Long id);
}
