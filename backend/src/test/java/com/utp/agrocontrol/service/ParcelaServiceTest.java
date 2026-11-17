package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.ParcelaRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.Parcela;
import com.utp.agrocontrol.model.Productor;
import com.utp.agrocontrol.repository.ParcelaRepository;
import com.utp.agrocontrol.repository.ProductorRepository;
import com.utp.agrocontrol.service.impl.ParcelaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de ParcelaServiceImpl (TDD).
 * Se probo primero la validacion del area en hectareas (debe ser positiva)
 * y la existencia del productor propietario, antes de completar la
 * implementacion definitiva del metodo registrar().
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del servicio ParcelaService (TDD)")
class ParcelaServiceTest {

    @Mock
    private ParcelaRepository parcelaRepository;

    @Mock
    private ProductorRepository productorRepository;

    @InjectMocks
    private ParcelaServiceImpl parcelaService;

    private Productor productor;
    private ParcelaRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        productor = new Productor(1L, "Julio", "Chunga", "41234567", "969111222", "julio@agro.pe", List.of());
        dtoValido = new ParcelaRequestDTO("Parcela El Progreso", "Valle del Chira, Piura", 3.5, 1L);
    }

    @Test
    @DisplayName("Debe registrar una parcela cuando los datos y el productor son validos")
    void debeRegistrarParcelaConDatosValidos() {
        when(productorRepository.findById(1L)).thenReturn(Optional.of(productor));
        when(parcelaRepository.save(any(Parcela.class))).thenAnswer(invocation -> {
            Parcela p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        Parcela resultado = parcelaService.registrar(dtoValido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getProductor().getDni()).isEqualTo("41234567");
        verify(parcelaRepository, times(1)).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Debe rechazar una parcela con area en hectareas negativa o cero")
    void debeRechazarParcelaConAreaInvalida() {
        ParcelaRequestDTO dtoInvalido = new ParcelaRequestDTO("Parcela X", "Piura", -2.0, 1L);

        assertThatThrownBy(() -> parcelaService.registrar(dtoInvalido))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("area");

        verify(parcelaRepository, never()).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Debe rechazar una parcela sin nombre")
    void debeRechazarParcelaSinNombre() {
        ParcelaRequestDTO dtoInvalido = new ParcelaRequestDTO("", "Piura", 3.0, 1L);

        assertThatThrownBy(() -> parcelaService.registrar(dtoInvalido))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("nombre");

        verify(parcelaRepository, never()).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el productor indicado no existe")
    void debeRechazarParcelaConProductorInexistente() {
        when(productorRepository.findById(99L)).thenReturn(Optional.empty());
        ParcelaRequestDTO dto = new ParcelaRequestDTO("Parcela X", "Piura", 3.0, 99L);

        assertThatThrownBy(() -> parcelaService.registrar(dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(parcelaRepository, never()).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Debe listar las parcelas de un productor especifico")
    void debeListarParcelasPorProductor() {
        Parcela parcela = new Parcela(1L, "Parcela El Progreso", "Piura", 3.5, productor, List.of());
        when(parcelaRepository.findByProductorId(1L)).thenReturn(List.of(parcela));

        List<Parcela> resultado = parcelaService.listarPorProductor(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Parcela El Progreso");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al buscar una parcela que no existe")
    void debeLanzarExcepcionSiParcelaNoExiste() {
        when(parcelaRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parcelaService.buscarPorId(50L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
