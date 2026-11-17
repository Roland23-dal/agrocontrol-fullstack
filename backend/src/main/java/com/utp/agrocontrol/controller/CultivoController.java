package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.model.Cultivo;
import com.utp.agrocontrol.security.Permisos;
import com.utp.agrocontrol.service.CultivoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints RESTful para el catalogo de Cultivos.
 * Base URL: /api/cultivos
 */
@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {

    private final CultivoService cultivoService;

    @Autowired
    public CultivoController(CultivoService cultivoService) {
        this.cultivoService = cultivoService;
    }

    @PreAuthorize(Permisos.LECTURA)

    @GetMapping
    public ResponseEntity<List<Cultivo>> listar() {
        return ResponseEntity.ok(cultivoService.listarTodos());
    }

    @PreAuthorize(Permisos.LECTURA)

    @GetMapping("/{id}")
    public ResponseEntity<Cultivo> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cultivoService.buscarPorId(id));
    }

    @PreAuthorize(Permisos.ESCRITURA)

    @PostMapping
    public ResponseEntity<Cultivo> registrar(@Valid @RequestBody Cultivo cultivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.registrar(cultivo));
    }

    @PreAuthorize(Permisos.ESCRITURA)

    @PutMapping("/{id}")
    public ResponseEntity<Cultivo> actualizar(@PathVariable Long id, @Valid @RequestBody Cultivo cultivo) {
        return ResponseEntity.ok(cultivoService.actualizar(id, cultivo));
    }

    @PreAuthorize(Permisos.ELIMINACION)

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cultivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
