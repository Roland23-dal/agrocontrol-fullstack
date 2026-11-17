package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.CampanaAgricola;
import com.utp.agrocontrol.model.Cosecha;
import com.utp.agrocontrol.model.Cultivo;
import com.utp.agrocontrol.model.EstadoCampana;
import com.utp.agrocontrol.model.Parcela;
import com.utp.agrocontrol.repository.CampanaAgricolaRepository;
import com.utp.agrocontrol.repository.CosechaRepository;
import com.utp.agrocontrol.repository.CultivoRepository;
import com.utp.agrocontrol.repository.ParcelaRepository;
import com.utp.agrocontrol.service.CampanaAgricolaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CampanaAgricolaServiceImpl implements CampanaAgricolaService {

    private final CampanaAgricolaRepository campanaRepository;
    private final ParcelaRepository parcelaRepository;
    private final CultivoRepository cultivoRepository;
    private final CosechaRepository cosechaRepository;

    @Autowired
    public CampanaAgricolaServiceImpl(CampanaAgricolaRepository campanaRepository,
                                       ParcelaRepository parcelaRepository,
                                       CultivoRepository cultivoRepository,
                                       CosechaRepository cosechaRepository) {
        this.campanaRepository = campanaRepository;
        this.parcelaRepository = parcelaRepository;
        this.cultivoRepository = cultivoRepository;
        this.cosechaRepository = cosechaRepository;
    }

    @Override
    public List<CampanaAgricola> listarTodas() {
        return campanaRepository.findAll();
    }

    @Override
    public CampanaAgricola buscarPorId(Long id) {
        return campanaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la campana con id " + id));
    }

    @Override
    public List<CampanaAgricola> listarPorParcela(Long parcelaId) {
        return campanaRepository.findByParcelaId(parcelaId);
    }

    @Override
    @Transactional
    public CampanaAgricola registrar(CampanaAgricola campana) {
        validar(campana);

        Parcela parcela = parcelaRepository.findById(campana.getParcela().getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la parcela indicada"));
        Cultivo cultivo = cultivoRepository.findById(campana.getCultivo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el cultivo indicado"));

        campana.setParcela(parcela);
        campana.setCultivo(cultivo);

        return campanaRepository.save(campana);
    }

    @Override
    @Transactional
    public CampanaAgricola actualizar(Long id, CampanaAgricola campana) {
        CampanaAgricola existente = buscarPorId(id);
        existente.setFechaInicio(campana.getFechaInicio());
        existente.setFechaFinEstimada(campana.getFechaFinEstimada());
        existente.setEstado(campana.getEstado());
        return campanaRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        CampanaAgricola campana = buscarPorId(id);
        campanaRepository.delete(campana);
    }

    // ---------------- Consultas JPQL (APF2) ----------------

    @Override
    public List<CampanaAgricola> listarPorEstado(EstadoCampana estado) {
        if (estado == null) {
            throw new InvalidDataException("Debe indicar el estado de la campana");
        }
        return campanaRepository.buscarPorEstado(estado);
    }

    @Override
    public List<CampanaAgricola> listarPorRangoDeFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new InvalidDataException("Debe indicar las fechas 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new InvalidDataException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        return campanaRepository.buscarPorRangoDeFechas(desde, hasta);
    }

    @Override
    public List<CampanaAgricola> listarPorNombreCultivo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new InvalidDataException("El nombre del cultivo es obligatorio para la busqueda");
        }
        return campanaRepository.buscarPorNombreCultivo(nombre.trim());
    }

    @Override
    public List<CampanaAgricola> listarAtrasadas() {
        return campanaRepository.buscarAtrasadas(EstadoCampana.EN_CURSO, LocalDate.now());
    }

    @Override
    public Map<String, Long> resumenPorEstado() {
        Map<String, Long> resumen = new LinkedHashMap<>();
        for (EstadoCampana estado : EstadoCampana.values()) {
            resumen.put(estado.name(), 0L);
        }
        for (Object[] fila : campanaRepository.contarPorEstado()) {
            resumen.put(((EstadoCampana) fila[0]).name(), (Long) fila[1]);
        }
        return resumen;
    }

    // ---------------- Operacion transaccional (APF2) ----------------

    /**
     * Finaliza una campana y registra su cosecha en UNA sola transaccion:
     * o se guardan ambas operaciones (cosecha + cambio de estado) o ninguna.
     * Si ocurre una RuntimeException (por ejemplo la campana ya estaba finalizada)
     * Spring revierte (rollback) todos los cambios realizados dentro del metodo.
     */
    @Override
    @Transactional
    public Cosecha finalizarCampana(Long campanaId, CosechaRequestDTO dto) {
        if (dto == null || dto.getFecha() == null) {
            throw new InvalidDataException("La fecha de la cosecha es obligatoria");
        }
        if (dto.getCantidadKg() == null || dto.getCantidadKg() <= 0) {
            throw new InvalidDataException("La cantidad en kilogramos debe ser un valor positivo");
        }

        CampanaAgricola campana = buscarPorId(campanaId);
        if (campana.getEstado() == EstadoCampana.FINALIZADA) {
            throw new InvalidDataException("La campana con id " + campanaId + " ya esta finalizada");
        }

        Cosecha cosecha = new Cosecha();
        cosecha.setFecha(dto.getFecha());
        cosecha.setCantidadKg(dto.getCantidadKg());
        cosecha.setObservacion(dto.getObservacion());
        cosecha.setCampana(campana);
        Cosecha guardada = cosechaRepository.save(cosecha);

        campana.setEstado(EstadoCampana.FINALIZADA);
        campanaRepository.save(campana);

        return guardada;
    }

    private void validar(CampanaAgricola campana) {
        if (campana.getFechaInicio() == null) {
            throw new InvalidDataException("La fecha de inicio de la campana es obligatoria");
        }
        if (campana.getParcela() == null || campana.getParcela().getId() == null) {
            throw new InvalidDataException("Debe indicar la parcela de la campana");
        }
        if (campana.getCultivo() == null || campana.getCultivo().getId() == null) {
            throw new InvalidDataException("Debe indicar el cultivo de la campana");
        }
    }
}
