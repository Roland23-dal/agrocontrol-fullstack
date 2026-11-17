package com.utp.agrocontrol.service.impl;

import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.Cultivo;
import com.utp.agrocontrol.repository.CultivoRepository;
import com.utp.agrocontrol.service.CultivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CultivoServiceImpl implements CultivoService {

    private final CultivoRepository cultivoRepository;

    @Autowired
    public CultivoServiceImpl(CultivoRepository cultivoRepository) {
        this.cultivoRepository = cultivoRepository;
    }

    @Override
    public List<Cultivo> listarTodos() {
        return cultivoRepository.findAll();
    }

    @Override
    public Cultivo buscarPorId(Long id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el cultivo con id " + id));
    }

    @Override
    @Transactional
    public Cultivo registrar(Cultivo cultivo) {
        if (cultivo.getNombre() == null || cultivo.getNombre().isBlank()) {
            throw new InvalidDataException("El nombre del cultivo es obligatorio");
        }
        return cultivoRepository.save(cultivo);
    }

    @Override
    @Transactional
    public Cultivo actualizar(Long id, Cultivo cultivo) {
        Cultivo existente = buscarPorId(id);
        existente.setNombre(cultivo.getNombre());
        existente.setVariedad(cultivo.getVariedad());
        existente.setCicloDiasEstimado(cultivo.getCicloDiasEstimado());
        return cultivoRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cultivo cultivo = buscarPorId(id);
        cultivoRepository.delete(cultivo);
    }
}
