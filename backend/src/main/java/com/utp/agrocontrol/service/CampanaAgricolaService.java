package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.model.CampanaAgricola;
import com.utp.agrocontrol.model.Cosecha;
import com.utp.agrocontrol.model.EstadoCampana;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CampanaAgricolaService {
    List<CampanaAgricola> listarTodas();
    CampanaAgricola buscarPorId(Long id);
    List<CampanaAgricola> listarPorParcela(Long parcelaId);
    CampanaAgricola registrar(CampanaAgricola campana);
    CampanaAgricola actualizar(Long id, CampanaAgricola campana);
    void eliminar(Long id);

    // Consultas JPQL (APF2)
    List<CampanaAgricola> listarPorEstado(EstadoCampana estado);
    List<CampanaAgricola> listarPorRangoDeFechas(LocalDate desde, LocalDate hasta);
    List<CampanaAgricola> listarPorNombreCultivo(String nombre);
    List<CampanaAgricola> listarAtrasadas();
    Map<String, Long> resumenPorEstado();

    // Operacion transaccional (APF2)
    Cosecha finalizarCampana(Long campanaId, CosechaRequestDTO dto);
}
