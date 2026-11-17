package com.utp.agrocontrol.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion de seguridad: levantan el contexto completo de Spring
 * (H2 + Spring Security + JWT) y verifican autenticacion (401) y autorizacion por rol (403).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas de integracion: Spring Security + JWT")
class SecurityIntegrationTest {

    private static final String CULTIVO_JSON =
            "{\"nombre\":\"Cafe\",\"variedad\":\"Typica\",\"cicloDiasEstimado\":300}";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private String login(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return mapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("La raiz (/) es publica")
    void raizEsPublica() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sin token un endpoint protegido responde 401")
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/productores")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un token invalido responde 401")
    void tokenInvalidoResponde401() throws Exception {
        mockMvc.perform(get("/api/productores").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con contrasena incorrecta responde 401")
    void loginInvalidoResponde401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login valido devuelve el token JWT y los roles")
    void loginValidoDevuelveToken() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("GUEST puede leer pero no crear (403)")
    void guestSoloLee() throws Exception {
        String token = login("guest", "guest123");
        mockMvc.perform(get("/api/cultivos").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/cultivos").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(CULTIVO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER puede crear pero no eliminar (403)")
    void userCreaPeroNoElimina() throws Exception {
        String token = login("user", "user123");
        mockMvc.perform(post("/api/cultivos").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(CULTIVO_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/productores/1").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN puede crear y eliminar")
    void adminCreaYElimina() throws Exception {
        String token = login("admin", "admin123");
        MvcResult creado = mockMvc.perform(post("/api/cultivos").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(CULTIVO_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        long id = mapper.readTree(creado.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/cultivos/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Solo ADMIN lista usuarios (@Secured)")
    void soloAdminListaUsuarios() throws Exception {
        String tokenUser = login("user", "user123");
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenUser)))
                .andExpect(status().isForbidden());

        String tokenAdmin = login("admin", "admin123");
        mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Registro crea un usuario con rol USER y devuelve su token; el duplicado responde 400")
    void registroYDuplicado() throws Exception {
        String username = "tester" + System.nanoTime();
        String body = "{\"username\":\"" + username + "\",\"password\":\"clave123\","
                + "\"email\":\"" + username + "@agro.pe\",\"nombreCompleto\":\"Usuario de Prueba\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Las consultas JPQL responden con datos (campanas EN_CURSO y resumen)")
    void consultasJpqlFuncionan() throws Exception {
        String token = login("guest", "guest123");
        mockMvc.perform(get("/api/campanas/estado/EN_CURSO").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("EN_CURSO"));
        mockMvc.perform(get("/api/campanas/resumen").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FINALIZADA").exists());
    }
}
