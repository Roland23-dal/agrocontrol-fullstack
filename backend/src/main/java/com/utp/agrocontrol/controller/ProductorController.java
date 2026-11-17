package com.utp.agrocontrol.controller;

import com.utp.agrocontrol.dto.ProductorRequestDTO;
import com.utp.agrocontrol.model.Productor;
import com.utp.agrocontrol.security.Permisos;
import com.utp.agrocontrol.service.ProductorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints RESTful para la gestion de Productores.
 * Base URL: /api/productores
 */
@RestController
@RequestMapping("/api/productores")
public class ProductorController {

    private final ProductorService productorService;

    @Autowired
    public ProductorController(ProductorService productorService) {
        this.productorService = productorService;
    }

    // GET /api/productores -> listar todos los productores
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping
    public ResponseEntity<List<Productor>> listar() {
        return ResponseEntity.ok(productorService.listarTodos());
    }

    // GET /api/productores/{id} -> obtener un productor por id
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/{id}")
    public ResponseEntity<Productor> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productorService.buscarPorId(id));
    }

    // POST /api/productores -> registrar un nuevo productor
    @PreAuthorize(Permisos.ESCRITURA)
    @PostMapping
    public ResponseEntity<Productor> registrar(@Valid @RequestBody ProductorRequestDTO dto) {
        Productor creado = productorService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // PUT /api/productores/{id} -> actualizar un productor existente
    @PreAuthorize(Permisos.ESCRITURA)
    @PutMapping("/{id}")
    public ResponseEntity<Productor> actualizar(@PathVariable Long id, @Valid @RequestBody ProductorRequestDTO dto) {
        return ResponseEntity.ok(productorService.actualizar(id, dto));
    }

    // DELETE /api/productores/{id} -> eliminar un productor
    @PreAuthorize(Permisos.ELIMINACION)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Consultas JPQL (APF2) ----------------

    // GET /api/productores/buscar?texto=chunga -> busqueda por nombres o apellidos
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/buscar")
    public ResponseEntity<List<Productor>> buscar(@RequestParam String texto) {
        return ResponseEntity.ok(productorService.buscarPorTexto(texto));
    }

    // GET /api/productores/con-parcelas-mayores-a?hectareas=4 -> productores con parcelas grandes
    @PreAuthorize(Permisos.LECTURA)
    @GetMapping("/con-parcelas-mayores-a")
    public ResponseEntity<List<Productor>> conParcelasMayoresA(@RequestParam Double hectareas) {
        return ResponseEntity.ok(productorService.listarConParcelasMayoresA(hectareas));
    }
}
