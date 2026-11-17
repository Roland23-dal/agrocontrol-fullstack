package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.dto.ProductorRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.Productor;
import com.utp.agrocontrol.repository.ProductorRepository;
import com.utp.agrocontrol.service.ProductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion de la logica de negocio para Productor.
 *
 * La dependencia de ProductorRepository se inyecta mediante Inversion de
 * Control (IoC): Spring construye este bean y le entrega automaticamente
 * la implementacion del repositorio (Dependency Injection por constructor).
 */
@Service
@Transactional(readOnly = true)
public class ProductorServiceImpl implements ProductorService {

    private final ProductorRepository productorRepository;

    @Autowired
    public ProductorServiceImpl(ProductorRepository productorRepository) {
        this.productorRepository = productorRepository;
    }

    @Override
    public List<Productor> listarTodos() {
        return productorRepository.findAll();
    }

    @Override
    public Productor buscarPorId(Long id) {
        return productorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el productor con id " + id));
    }

    @Override
    @Transactional
    public Productor registrar(ProductorRequestDTO dto) {
        validarDatos(dto);

        if (productorRepository.existsByDni(dto.getDni())) {
            throw new InvalidDataException("Ya existe un productor registrado con el DNI " + dto.getDni());
        }

        Productor productor = new Productor();
        productor.setNombres(dto.getNombres());
        productor.setApellidos(dto.getApellidos());
        productor.setDni(dto.getDni());
        productor.setTelefono(dto.getTelefono());
        productor.setEmail(dto.getEmail());

        return productorRepository.save(productor);
    }

    @Override
    @Transactional
    public Productor actualizar(Long id, ProductorRequestDTO dto) {
        validarDatos(dto);
        Productor productor = buscarPorId(id);

        if (!productor.getDni().equals(dto.getDni()) && productorRepository.existsByDni(dto.getDni())) {
            throw new InvalidDataException("Ya existe otro productor registrado con el DNI " + dto.getDni());
        }

        productor.setNombres(dto.getNombres());
        productor.setApellidos(dto.getApellidos());
        productor.setDni(dto.getDni());
        productor.setTelefono(dto.getTelefono());
        productor.setEmail(dto.getEmail());

        return productorRepository.save(productor);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Productor productor = buscarPorId(id);
        productorRepository.delete(productor);
    }

    /**
     * Validacion de entradas a nivel de servicio (complementa las anotaciones
     * de Bean Validation del DTO). Este metodo fue el primero en escribirse
     * durante el ciclo de TDD: ver documentacion de TDD en el README.
     */
    private void validarDatos(ProductorRequestDTO dto) {
        if (dto == null) {
            throw new InvalidDataException("Los datos del productor no pueden ser nulos");
        }
        if (dto.getDni() == null || !dto.getDni().matches("\\d{8}")) {
            throw new InvalidDataException("El DNI debe tener exactamente 8 digitos numericos");
        }
        if (dto.getNombres() == null || dto.getNombres().isBlank()) {
            throw new InvalidDataException("El nombre del productor es obligatorio");
        }
        if (dto.getApellidos() == null || dto.getApellidos().isBlank()) {
            throw new InvalidDataException("El apellido del productor es obligatorio");
        }
    }

    @Override
    public List<Productor> buscarPorTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new InvalidDataException("El texto de busqueda es obligatorio");
        }
        return productorRepository.buscarPorNombreOApellido(texto.trim());
    }

    @Override
    public List<Productor> listarConParcelasMayoresA(Double hectareas) {
        if (hectareas == null || hectareas < 0) {
            throw new InvalidDataException("Las hectareas deben ser un valor igual o mayor a cero");
        }
        return productorRepository.buscarConParcelasMayoresA(hectareas);
    }
}
