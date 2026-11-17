package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.dto.UsuarioResponseDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.NombreRol;
import com.utp.agrocontrol.model.Rol;
import com.utp.agrocontrol.model.Usuario;
import com.utp.agrocontrol.repository.RolRepository;
import com.utp.agrocontrol.repository.UsuarioRepository;
import com.utp.agrocontrol.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Gestion de usuarios y roles (operaciones reservadas al ADMIN, salvo el perfil propio). */
@Service
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::aDto).toList();
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {
        return aDto(obtener(id));
    }

    @Override
    public UsuarioResponseDTO obtenerPerfil(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario " + username));
        return aDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO asignarRoles(Long id, Set<NombreRol> nombres) {
        if (nombres == null || nombres.isEmpty()) {
            throw new InvalidDataException("Debe indicar al menos un rol");
        }
        Usuario usuario = obtener(id);
        Set<Rol> roles = new HashSet<>();
        for (NombreRol nombre : nombres) {
            Rol rol = rolRepository.findByNombre(nombre)
                    .orElseThrow(() -> new ResourceNotFoundException("No existe el rol " + nombre));
            roles.add(rol);
        }
        usuario.setRoles(roles);
        return aDto(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(obtener(id));
    }

    private Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario con id " + id));
    }

    private UsuarioResponseDTO aDto(Usuario u) {
        List<String> roles = u.getRoles().stream()
                .map(r -> "ROLE_" + r.getNombre().name())
                .sorted()
                .toList();
        return new UsuarioResponseDTO(u.getId(), u.getUsername(), u.getEmail(),
                u.getNombreCompleto(), u.isEnabled(), roles);
    }
}
