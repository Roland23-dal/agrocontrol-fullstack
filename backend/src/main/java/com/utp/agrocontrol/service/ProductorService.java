package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.ProductorRequestDTO;
import com.utp.agrocontrol.model.Productor;

import java.util.List;

public interface ProductorService {
    List<Productor> listarTodos();
    Productor buscarPorId(Long id);
    Productor registrar(ProductorRequestDTO dto);
    Productor actualizar(Long id, ProductorRequestDTO dto);
    void eliminar(Long id);

    // Consultas JPQL (APF2)
    List<Productor> buscarPorTexto(String texto);
    List<Productor> listarConParcelasMayoresA(Double hectareas);
}
