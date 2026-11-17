package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.ParcelaRequestDTO;
import com.utp.agrocontrol.model.Parcela;

import java.util.List;

public interface ParcelaService {
    List<Parcela> listarTodas();
    Parcela buscarPorId(Long id);
    List<Parcela> listarPorProductor(Long productorId);
    Parcela registrar(ParcelaRequestDTO dto);
    Parcela actualizar(Long id, ParcelaRequestDTO dto);
    void eliminar(Long id);

    // Consultas JPQL y operacion transaccional (APF2)
    List<Parcela> listarPorAreaMinima(Double hectareas);
    List<Parcela> buscarPorUbicacion(String ubicacion);
    Double calcularAreaTotalPorProductor(Long productorId);
    Parcela transferir(Long parcelaId, Long nuevoProductorId);
}
