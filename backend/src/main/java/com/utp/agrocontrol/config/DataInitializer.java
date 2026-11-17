package com.utp.agrocontrol.config;

import com.utp.agrocontrol.model.NombreRol;
import com.utp.agrocontrol.model.Rol;
import com.utp.agrocontrol.model.Usuario;
import com.utp.agrocontrol.repository.RolRepository;
import com.utp.agrocontrol.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Carga inicial de seguridad: crea los roles ADMIN, USER y GUEST y tres usuarios
 * de prueba (contrasenas cifradas con BCrypt) si todavia no existen.
 * Es solo para desarrollo/demostracion; en produccion se deben cambiar estas credenciales.
 *
 *   admin / admin123  -> ADMIN
 *   user  / user123   -> USER
 *   guest / guest123  -> GUEST
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (NombreRol nombre : NombreRol.values()) {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                rolRepository.save(new Rol(nombre));
            }
        }

        crearUsuarioSiNoExiste("admin", "admin123", "admin@agrocontrol.pe", "Administrador AgroControl", NombreRol.ADMIN);
        crearUsuarioSiNoExiste("user", "user123", "user@agrocontrol.pe", "Tecnico de Campo", NombreRol.USER);
        crearUsuarioSiNoExiste("guest", "guest123", "guest@agrocontrol.pe", "Invitado Consultor", NombreRol.GUEST);

        log.info("Seguridad inicializada: roles {} y usuarios de prueba (admin, user, guest)", Arrays.toString(NombreRol.values()));
    }

    private void crearUsuarioSiNoExiste(String username, String password, String email,
                                        String nombreCompleto, NombreRol nombreRol) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEmail(email);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setEnabled(true);
        usuario.setRoles(roles);
        usuarioRepository.save(usuario);
    }
}
