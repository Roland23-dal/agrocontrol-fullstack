package com.utp.agrocontrol.security;

import com.utp.agrocontrol.model.Usuario;
import com.utp.agrocontrol.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserDetailsService personalizado: recupera los usuarios desde la base de datos
 * (tabla usuarios) y los convierte al formato que entiende Spring Security.
 * Cada rol se expone como autoridad con prefijo ROLE_ (ROLE_ADMIN, ROLE_USER, ROLE_GUEST).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<GrantedAuthority> autoridades = usuario.getRoles().stream()
                .<GrantedAuthority>map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre().name()))
                .toList();

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!usuario.isEnabled())
                .authorities(autoridades)
                .build();
    }
}
