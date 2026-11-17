package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.model.CampanaAgricola;
import com.utp.agrocontrol.model.Cosecha;
import com.utp.agrocontrol.model.EstadoCampana;
import com.utp.agrocontrol.security.Permisos;
import com.utp.agrocontrol.service.CampanaAgricolaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Endpoints RESTful para las Campanas Agricolas (el proceso productivo
 * de un cultivo dentro de una parcela).
 * Base URL: /api/campanas
 */
@RestController
@RequestMapping("/api/campanas")
public class CampanaAgricolaController {

    private final CampanaAgricolaService campanaService;

    @Autowired
    public CampanaAgricolaController(CampanaAgricolaService campanaService) {
        this.campanaService = campanaService;
    }

    @PreAuthorize(Permisos.LECTURA)

    @GetMapping
    public ResponseEntity<List<CampanaAgricola>> listar(@RequestParam(required = false) Long parcelaId) {
        if (parcelaId != null) {
            return ResponseEntity.ok(campanaService.listarPorParcela(parcelaId));
        }
        return ResponseEntity.ok(campanaService.listarTodas());
    }

    @PreAuthorize(Permisos.LECTURA)

    @GetMapping("/{id}")
    public ResponseEntity<CampanaAgricola> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campanaService.buscarPorId(id));
    }

    @PreAuthorize(Permisos.ESCRITURA)

    @PostMapping
    public ResponseEntity<CampanaAgricola> registrar(@RequestBody CampanaAgricola campana) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campanaService.registrar(campana));
    }

    @PreAuthorize(Permisos.ESCRITURA)

    @PutMapping("/{id}")
    public ResponseEntity<CampanaAgricola> actualizar(@PathVariable Long id, @RequestBody CampanaAgricola campana) {
        return ResponseEntity.ok(campanaService.actualizar(id, campana));
    }

    @PreAuthorize(Permisos.ELIMINACION)

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        campanaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Consultas JPQL y transaccion (APF2) ----------------

    // GET /api/campanas/estado/EN_CURSO
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CampanaAgricola>> porEstado(@PathVariable EstadoCampana estado) {
        return ResponseEntity.ok(campanaService.listarPorEstado(estado));
    }

    // GET /api/campanas/fechas?desde=2026-01-01&hasta=2026-12-31
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/fechas")
    public ResponseEntity<List<CampanaAgricola>> porFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(campanaService.listarPorRangoDeFechas(desde, hasta));
    }

    // GET /api/campanas/cultivo?nombre=arroz
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/cultivo")
    public ResponseEntity<List<CampanaAgricola>> porCultivo(@RequestParam String nombre) {
        return ResponseEntity.ok(campanaService.listarPorNombreCultivo(nombre));
    }

    // GET /api/campanas/atrasadas -> campanas EN_CURSO con fecha fin estimada vencida
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/atrasadas")
    public ResponseEntity<List<CampanaAgricola>> atrasadas() {
        return ResponseEntity.ok(campanaService.listarAtrasadas());
    }

    // GET /api/campanas/resumen -> cantidad de campanas por estado
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Long>> resumen() {
        return ResponseEntity.ok(campanaService.resumenPorEstado());
    }

    // POST /api/campanas/{id}/finalizar -> registra la cosecha y finaliza la campana (@Transactional)
    @PreAuthorize(Permisos.ESCRITURA)
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<Cosecha> finalizar(@PathVariable Long id, @Valid @RequestBody CosechaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campanaService.finalizarCampana(id, dto));
    }
}
