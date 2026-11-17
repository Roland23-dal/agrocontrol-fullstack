package com.utp.agrocontrol.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de verificacion rapida de que la API esta activa (health-check simple).
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("aplicacion", "AgroControl API");
        info.put("estado", "activo");
        info.put("descripcion", "Sistema Web para Gestion y Trazabilidad de Produccion Agricola");
        info.put("seguridad", "JWT (Authorization: Bearer <token>) - obtener token en POST /api/auth/login");
        info.put("endpoints", new String[]{
                "/api/auth/register", "/api/auth/login", "/api/productores", "/api/parcelas",
                "/api/cultivos", "/api/campanas", "/api/cosechas", "/api/usuarios"
        });
        return info;
    }
}
