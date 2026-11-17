package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.CampanaAgricola;
import com.utp.agrocontrol.model.Cosecha;
import com.utp.agrocontrol.model.EstadoCampana;
import com.utp.agrocontrol.repository.CampanaAgricolaRepository;
import com.utp.agrocontrol.repository.CosechaRepository;
import com.utp.agrocontrol.service.CosechaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Logica de negocio (CRUD) para las cosechas de una campana agricola. */
@Service
@Transactional(readOnly = true)
public class CosechaServiceImpl implements CosechaService {

    private final CosechaRepository cosechaRepository;
    private final CampanaAgricolaRepository campanaRepository;

    @Autowired
    public CosechaServiceImpl(CosechaRepository cosechaRepository, CampanaAgricolaRepository campanaRepository) {
        this.cosechaRepository = cosechaRepository;
        this.campanaRepository = campanaRepository;
    }

    @Override
    public List<Cosecha> listarTodas() {
        return cosechaRepository.findAll();
    }

    @Override
    public Cosecha buscarPorId(Long id) {
        return cosechaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la cosecha con id " + id));
    }

    @Override
    public List<Cosecha> listarPorCampana(Long campanaId) {
        return cosechaRepository.findByCampanaId(campanaId);
    }

    @Override
    @Transactional
    public Cosecha registrar(CosechaRequestDTO dto) {
        validarDatos(dto);
        if (dto.getCampanaId() == null) {
            throw new InvalidDataException("Debe indicar el id de la campana de la cosecha");
        }
        CampanaAgricola campana = campanaRepository.findById(dto.getCampanaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la campana con id " + dto.getCampanaId()));
        if (campana.getEstado() == EstadoCampana.PLANIFICADA) {
            throw new InvalidDataException("No se puede registrar una cosecha en una campana PLANIFICADA");
        }

        Cosecha cosecha = new Cosecha();
        cosecha.setFecha(dto.getFecha());
        cosecha.setCantidadKg(dto.getCantidadKg());
        cosecha.setObservacion(dto.getObservacion());
        cosecha.setCampana(campana);
        return cosechaRepository.save(cosecha);
    }

    @Override
    @Transactional
    public Cosecha actualizar(Long id, CosechaRequestDTO dto) {
        validarDatos(dto);
        Cosecha cosecha = buscarPorId(id);
        cosecha.setFecha(dto.getFecha());
        cosecha.setCantidadKg(dto.getCantidadKg());
        cosecha.setObservacion(dto.getObservacion());
        return cosechaRepository.save(cosecha);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cosecha cosecha = buscarPorId(id);
        cosechaRepository.delete(cosecha);
    }

    @Override
    public List<Cosecha> listarPorRangoDeFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new InvalidDataException("Debe indicar las fechas 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new InvalidDataException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        return cosechaRepository.buscarPorRangoDeFechas(desde, hasta);
    }

    @Override
    public Double calcularTotalKgPorCampana(Long campanaId) {
        if (!campanaRepository.existsById(campanaId)) {
            throw new ResourceNotFoundException("No se encontro la campana con id " + campanaId);
        }
        return cosechaRepository.calcularTotalKgPorCampana(campanaId);
    }

    private void validarDatos(CosechaRequestDTO dto) {
        if (dto == null) {
            throw new InvalidDataException("Los datos de la cosecha no pueden ser nulos");
        }
        if (dto.getFecha() == null) {
            throw new InvalidDataException("La fecha de la cosecha es obligatoria");
        }
        if (dto.getCantidadKg() == null || dto.getCantidadKg() <= 0) {
            throw new InvalidDataException("La cantidad en kilogramos debe ser un valor positivo");
        }
    }
}
