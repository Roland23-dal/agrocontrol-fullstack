package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.dto.AuthResponseDTO;
import com.utp.agrocontrol.dto.LoginRequestDTO;
import com.utp.agrocontrol.dto.RegisterRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.model.NombreRol;
import com.utp.agrocontrol.model.Rol;
import com.utp.agrocontrol.model.Usuario;
import com.utp.agrocontrol.repository.RolRepository;
import com.utp.agrocontrol.repository.UsuarioRepository;
import com.utp.agrocontrol.security.JwtService;
import com.utp.agrocontrol.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registro y autenticacion de usuarios.
 *  - registrar: valida unicidad, cifra la contrasena con BCrypt, asigna el rol USER
 *    (un registro publico nunca puede auto-asignarse ADMIN) y devuelve un token JWT.
 *  - login: autentica con el AuthenticationManager (UserDetailsService + BCrypt)
 *    y, si las credenciales son correctas, genera el token JWT.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponseDTO registrar(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new InvalidDataException("Ya existe un usuario con el nombre '" + dto.getUsername() + "'");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new InvalidDataException("Ya existe un usuario registrado con el email " + dto.getEmail());
        }

        Rol rolUser = rolRepository.findByNombre(NombreRol.USER)
                .orElseGet(() -> rolRepository.save(new Rol(NombreRol.USER)));
        Set<Rol> roles = new HashSet<>();
        roles.add(rolUser);

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEmail(dto.getEmail());
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setEnabled(true);
        usuario.setRoles(roles);
        usuarioRepository.save(usuario);

        // Se autentica con las credenciales recien registradas para devolver el token
        return login(new LoginRequestDTO(dto.getUsername(), dto.getPassword()));
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        UserDetails usuario = (UserDetails) autenticacion.getPrincipal();
        String token = jwtService.generarToken(usuario);
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthResponseDTO(token, usuario.getUsername(), roles, jwtService.getExpirationMs());
    }
}
