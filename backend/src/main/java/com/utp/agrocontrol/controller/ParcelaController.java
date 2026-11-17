package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.dto.ParcelaRequestDTO;
import com.utp.agrocontrol.dto.TransferirParcelaDTO;
import com.utp.agrocontrol.model.Parcela;
import com.utp.agrocontrol.security.Permisos;
import com.utp.agrocontrol.service.ParcelaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints RESTful para la gestion de Parcelas.
 * Base URL: /api/parcelas
 */
@RestController
@RequestMapping("/api/parcelas")
public class ParcelaController {

    private final ParcelaService parcelaService;

    @Autowired
    public ParcelaController(ParcelaService parcelaService) {
        this.parcelaService = parcelaService;
    }

    // GET /api/parcelas -> listar todas, o /api/parcelas?productorId=1 -> filtrar por productor
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping
    public ResponseEntity<List<Parcela>> listar(@RequestParam(required = false) Long productorId) {
        if (productorId != null) {
            return ResponseEntity.ok(parcelaService.listarPorProductor(productorId));
        }
        return ResponseEntity.ok(parcelaService.listarTodas());
    }

    @PreAuthorize(Permisos.LECTURA)

    @GetMapping("/{id}")
    public ResponseEntity<Parcela> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(parcelaService.buscarPorId(id));
    }

    @PreAuthorize(Permisos.ESCRITURA)

    @PostMapping
    public ResponseEntity<Parcela> registrar(@Valid @RequestBody ParcelaRequestDTO dto) {
        Parcela creada = parcelaService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PreAuthorize(Permisos.ESCRITURA)

    @PutMapping("/{id}")
    public ResponseEntity<Parcela> actualizar(@PathVariable Long id, @Valid @RequestBody ParcelaRequestDTO dto) {
        return ResponseEntity.ok(parcelaService.actualizar(id, dto));
    }

    @PreAuthorize(Permisos.ELIMINACION)

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        parcelaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Consultas JPQL y transaccion (APF2) ----------------

    // GET /api/parcelas/area-minima?hectareas=4
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/area-minima")
    public ResponseEntity<List<Parcela>> porAreaMinima(@RequestParam Double hectareas) {
        return ResponseEntity.ok(parcelaService.listarPorAreaMinima(hectareas));
    }

    // GET /api/parcelas/buscar?ubicacion=piura
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/buscar")
    public ResponseEntity<List<Parcela>> buscarPorUbicacion(@RequestParam String ubicacion) {
        return ResponseEntity.ok(parcelaService.buscarPorUbicacion(ubicacion));
    }

    // GET /api/parcelas/productor/{productorId}/area-total
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/productor/{productorId}/area-total")
    public ResponseEntity<Map<String, Object>> areaTotal(@PathVariable Long productorId) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("productorId", productorId);
        respuesta.put("areaTotalHectareas", parcelaService.calcularAreaTotalPorProductor(productorId));
        return ResponseEntity.ok(respuesta);
    }

    // PUT /api/parcelas/{id}/transferir  body: {"nuevoProductorId": 2}   (@Transactional)
    @PreAuthorize(Permisos.ESCRITURA)
    @PutMapping("/{id}/transferir")
    public ResponseEntity<Parcela> transferir(@PathVariable Long id, @Valid @RequestBody TransferirParcelaDTO dto) {
        return ResponseEntity.ok(parcelaService.transferir(id, dto.getNuevoProductorId()));
    }
}
