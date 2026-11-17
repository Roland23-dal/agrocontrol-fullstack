package com.utp.agrocontrol.service;

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
import com.utp.agrocontrol.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de AuthServiceImpl (registro y login) con Mockito. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del servicio de autenticacion")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Debe rechazar el registro si el username ya existe")
    void debeRechazarUsernameDuplicado() {
        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);
        RegisterRequestDTO dto = new RegisterRequestDTO("admin", "123456", "a@a.pe", "Admin");

        assertThatThrownBy(() -> authService.registrar(dto))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("admin");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe rechazar el registro si el email ya existe")
    void debeRechazarEmailDuplicado() {
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("a@a.pe")).thenReturn(true);
        RegisterRequestDTO dto = new RegisterRequestDTO("nuevo", "123456", "a@a.pe", "Nuevo");

        assertThatThrownBy(() -> authService.registrar(dto))
                .isInstanceOf(InvalidDataException.class);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe registrar con rol USER, contrasena cifrada y devolver el token")
    void debeRegistrarUsuarioConRolUserYPasswordCifrada() {
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("n@a.pe")).thenReturn(false);
        when(rolRepository.findByNombre(NombreRol.USER)).thenReturn(Optional.of(new Rol(NombreRol.USER)));
        when(passwordEncoder.encode("123456")).thenReturn("HASH_BCRYPT");

        UserDetails detalles = User.withUsername("nuevo").password("HASH_BCRYPT").roles("USER").build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(detalles);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generarToken(detalles)).thenReturn("TOKEN");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponseDTO respuesta = authService.registrar(
                new RegisterRequestDTO("nuevo", "123456", "n@a.pe", "Nuevo Usuario"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("HASH_BCRYPT");
        assertThat(captor.getValue().getRoles()).extracting(Rol::getNombre).containsExactly(NombreRol.USER);
        assertThat(respuesta.getToken()).isEqualTo("TOKEN");
        assertThat(respuesta.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Debe devolver un token JWT cuando las credenciales son correctas")
    void debeHacerLoginYDevolverToken() {
        UserDetails detalles = User.withUsername("admin").password("x").roles("ADMIN").build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(detalles);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generarToken(detalles)).thenReturn("TOKEN_ADMIN");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponseDTO respuesta = authService.login(new LoginRequestDTO("admin", "admin123"));

        assertThat(respuesta.getToken()).isEqualTo("TOKEN_ADMIN");
        assertThat(respuesta.getTipo()).isEqualTo("Bearer");
        assertThat(respuesta.getUsername()).isEqualTo("admin");
        assertThat(respuesta.getRoles()).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Debe propagar BadCredentialsException cuando la contrasena es incorrecta")
    void debePropagarCredencialesInvalidas() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("admin", "mala")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService);
    }
}
