package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.model.Cosecha;

import java.time.LocalDate;
import java.util.List;

public interface CosechaService {
    List<Cosecha> listarTodas();
    Cosecha buscarPorId(Long id);
    List<Cosecha> listarPorCampana(Long campanaId);
    Cosecha registrar(CosechaRequestDTO dto);
    Cosecha actualizar(Long id, CosechaRequestDTO dto);
    void eliminar(Long id);

    // Consultas JPQL (APF2)
    List<Cosecha> listarPorRangoDeFechas(LocalDate desde, LocalDate hasta);
    Double calcularTotalKgPorCampana(Long campanaId);
}
