package com.utp.agrocontrol.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Configuracion central de Spring Security (API stateless con JWT).
 *
 *  - /, /api/auth/** y la consola H2 son publicos.
 *  - Cualquier otro endpoint exige un token JWT valido.
 *  - La autorizacion fina por rol se define en los controladores con
 *    @PreAuthorize / @Secured (habilitados con @EnableMethodSecurity).
 *  - Sin sesiones HTTP (STATELESS) y sin CSRF, porque la API no usa cookies.
 *  - APF3: CORS habilitado para el front-end Angular (por defecto http://localhost:4200),
 *    configurable con la propiedad app.cors.allowed-origins.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           UserDetailsService userDetailsService,
                                           AuthenticationProvider authenticationProvider,
                                           ObjectMapper objectMapper) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
            .csrf(csrf -> csrf.disable())
            // APF3: permite que el navegador llame a la API desde el front-end Angular (otro origen)
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // La consola H2 se muestra dentro de un <frame>; se permite solo mismo origen
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                // Las solicitudes preflight (OPTIONS) del navegador no llevan token
                .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/api/auth/**")).permitAll()
                .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                // 401: sin token, token invalido o expirado
                .authenticationEntryPoint((request, response, e) -> escribirError(response, objectMapper,
                        HttpStatus.UNAUTHORIZED,
                        "No autenticado: envie un token JWT valido en el header Authorization (Bearer <token>)"))
                // 403: autenticado pero sin el rol necesario
                .accessDeniedHandler((request, response, e) -> escribirError(response, objectMapper,
                        HttpStatus.FORBIDDEN,
                        "Acceso denegado: su rol no tiene permisos para esta operacion")))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * APF3 - CORS: autoriza al front-end Angular a consumir la API.
     * Se permiten los metodos REST usados por el front y el header Authorization (JWT).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String[] origenesPermitidos) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenesPermitidos));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Cifrado de contrasenas con BCrypt (hash con sal; nunca se guarda texto plano). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Proveedor que valida credenciales usando CustomUserDetailsService + BCrypt. */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void escribirError(HttpServletResponse response, ObjectMapper mapper,
                               HttpStatus status, String mensaje) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", mensaje);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), body);
    }
}
