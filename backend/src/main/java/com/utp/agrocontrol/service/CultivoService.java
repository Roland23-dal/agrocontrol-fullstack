package com.utp.agrocontrol.service;

import com.utp.agrocontrol.model.Cultivo;

import java.util.List;

public interface CultivoService {
    List<Cultivo> listarTodos();
    Cultivo buscarPorId(Long id);
    Cultivo registrar(Cultivo cultivo);
    Cultivo actualizar(Long id, Cultivo cultivo);
    void eliminar(Long id);
}
