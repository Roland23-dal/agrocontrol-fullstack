package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.dto.ParcelaRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.Parcela;
import com.utp.agrocontrol.model.Productor;
import com.utp.agrocontrol.repository.ParcelaRepository;
import com.utp.agrocontrol.repository.ProductorRepository;
import com.utp.agrocontrol.service.ParcelaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion de la logica de negocio para Parcela.
 * Ambos repositorios se inyectan por constructor (IoC / Dependency Injection).
 */
@Service
@Transactional(readOnly = true)
public class ParcelaServiceImpl implements ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final ProductorRepository productorRepository;

    @Autowired
    public ParcelaServiceImpl(ParcelaRepository parcelaRepository, ProductorRepository productorRepository) {
        this.parcelaRepository = parcelaRepository;
        this.productorRepository = productorRepository;
    }

    @Override
    public List<Parcela> listarTodas() {
        return parcelaRepository.findAll();
    }

    @Override
    public Parcela buscarPorId(Long id) {
        return parcelaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la parcela con id " + id));
    }

    @Override
    public List<Parcela> listarPorProductor(Long productorId) {
        return parcelaRepository.findByProductorId(productorId);
    }

    @Override
    @Transactional
    public Parcela registrar(ParcelaRequestDTO dto) {
        validarDatos(dto);

        Productor productor = productorRepository.findById(dto.getProductorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el productor con id " + dto.getProductorId()));

        Parcela parcela = new Parcela();
        parcela.setNombre(dto.getNombre());
        parcela.setUbicacion(dto.getUbicacion());
        parcela.setAreaHectareas(dto.getAreaHectareas());
        parcela.setProductor(productor);

        return parcelaRepository.save(parcela);
    }

    @Override
    @Transactional
    public Parcela actualizar(Long id, ParcelaRequestDTO dto) {
        validarDatos(dto);
        Parcela parcela = buscarPorId(id);

        Productor productor = productorRepository.findById(dto.getProductorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el productor con id " + dto.getProductorId()));

        parcela.setNombre(dto.getNombre());
        parcela.setUbicacion(dto.getUbicacion());
        parcela.setAreaHectareas(dto.getAreaHectareas());
        parcela.setProductor(productor);

        return parcelaRepository.save(parcela);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Parcela parcela = buscarPorId(id);
        parcelaRepository.delete(parcela);
    }

    private void validarDatos(ParcelaRequestDTO dto) {
        if (dto == null) {
            throw new InvalidDataException("Los datos de la parcela no pueden ser nulos");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new InvalidDataException("El nombre de la parcela es obligatorio");
        }
        if (dto.getAreaHectareas() == null || dto.getAreaHectareas() <= 0) {
            throw new InvalidDataException("El area en hectareas debe ser un valor positivo");
        }
        if (dto.getProductorId() == null) {
            throw new InvalidDataException("Debe indicar el id del productor propietario de la parcela");
        }
    }

    @Override
    public List<Parcela> listarPorAreaMinima(Double hectareas) {
        if (hectareas == null || hectareas < 0) {
            throw new InvalidDataException("Las hectareas deben ser un valor igual o mayor a cero");
        }
        return parcelaRepository.buscarPorAreaMinima(hectareas);
    }

    @Override
    public List<Parcela> buscarPorUbicacion(String ubicacion) {
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new InvalidDataException("La ubicacion de busqueda es obligatoria");
        }
        return parcelaRepository.buscarPorUbicacion(ubicacion.trim());
    }

    @Override
    public Double calcularAreaTotalPorProductor(Long productorId) {
        if (!productorRepository.existsById(productorId)) {
            throw new ResourceNotFoundException("No se encontro el productor con id " + productorId);
        }
        return parcelaRepository.calcularAreaTotalPorProductor(productorId);
    }

    /**
     * Operacion transaccional: cambia el propietario de una parcela.
     * Si cualquier validacion falla (RuntimeException) se hace rollback y la
     * parcela conserva a su productor original.
     */
    @Override
    @Transactional
    public Parcela transferir(Long parcelaId, Long nuevoProductorId) {
        if (nuevoProductorId == null) {
            throw new InvalidDataException("Debe indicar el id del nuevo productor");
        }
        Parcela parcela = buscarPorId(parcelaId);
        Productor nuevo = productorRepository.findById(nuevoProductorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el productor con id " + nuevoProductorId));

        if (parcela.getProductor().getId().equals(nuevoProductorId)) {
            throw new InvalidDataException("La parcela ya pertenece a ese productor");
        }
        parcela.setProductor(nuevo);
        return parcelaRepository.save(parcela);
    }
}
