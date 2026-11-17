package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.model.Cosecha;
import com.utp.agrocontrol.security.Permisos;
import com.utp.agrocontrol.service.CosechaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints RESTful para las Cosechas de una campana agricola.
 * Base URL: /api/cosechas
 */
@RestController
@RequestMapping("/api/cosechas")
public class CosechaController {

    private final CosechaService cosechaService;

    public CosechaController(CosechaService cosechaService) {
        this.cosechaService = cosechaService;
    }

    // GET /api/cosechas  o  /api/cosechas?campanaId=1
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping
    public ResponseEntity<List<Cosecha>> listar(@RequestParam(required = false) Long campanaId) {
        if (campanaId != null) {
            return ResponseEntity.ok(cosechaService.listarPorCampana(campanaId));
        }
        return ResponseEntity.ok(cosechaService.listarTodas());
    }

    // GET /api/cosechas/fechas?desde=2026-01-01&hasta=2026-12-31   (JPQL)
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/fechas")
    public ResponseEntity<List<Cosecha>> listarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(cosechaService.listarPorRangoDeFechas(desde, hasta));
    }

    // GET /api/cosechas/campana/{campanaId}/total   (JPQL: SUM)
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/campana/{campanaId}/total")
    public ResponseEntity<Map<String, Object>> totalPorCampana(@PathVariable Long campanaId) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("campanaId", campanaId);
        respuesta.put("totalKg", cosechaService.calcularTotalKgPorCampana(campanaId));
        return ResponseEntity.ok(respuesta);
    }

    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/{id}")
    public ResponseEntity<Cosecha> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cosechaService.buscarPorId(id));
    }

    @PreAuthorize(Permisos.ESCRITURA)
    @PostMapping
    public ResponseEntity<Cosecha> registrar(@Valid @RequestBody CosechaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cosechaService.registrar(dto));
    }

    @PreAuthorize(Permisos.ESCRITURA)
    @PutMapping("/{id}")
    public ResponseEntity<Cosecha> actualizar(@PathVariable Long id, @Valid @RequestBody CosechaRequestDTO dto) {
        return ResponseEntity.ok(cosechaService.actualizar(id, dto));
    }

    @PreAuthorize(Permisos.ELIMINACION)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cosechaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
