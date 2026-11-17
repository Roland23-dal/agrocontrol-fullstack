package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.CosechaRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.CampanaAgricola;
import com.utp.agrocontrol.model.Cosecha;
import com.utp.agrocontrol.model.EstadoCampana;
import com.utp.agrocontrol.repository.CampanaAgricolaRepository;
import com.utp.agrocontrol.repository.CosechaRepository;
import com.utp.agrocontrol.repository.CultivoRepository;
import com.utp.agrocontrol.repository.ParcelaRepository;
import com.utp.agrocontrol.service.impl.CampanaAgricolaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas de la logica transaccional y de las consultas del servicio de campanas (APF2). */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del servicio CampanaAgricolaService (APF2)")
class CampanaAgricolaServiceTest {

    @Mock private CampanaAgricolaRepository campanaRepository;
    @Mock private ParcelaRepository parcelaRepository;
    @Mock private CultivoRepository cultivoRepository;
    @Mock private CosechaRepository cosechaRepository;

    @InjectMocks
    private CampanaAgricolaServiceImpl campanaService;

    private CampanaAgricola campanaEnCurso;

    @BeforeEach
    void setUp() {
        campanaEnCurso = new CampanaAgricola();
        campanaEnCurso.setId(1L);
        campanaEnCurso.setFechaInicio(LocalDate.of(2026, 6, 1));
        campanaEnCurso.setEstado(EstadoCampana.EN_CURSO);
    }

    @Test
    @DisplayName("Debe registrar la cosecha y finalizar la campana en la misma operacion")
    void debeFinalizarCampanaYRegistrarCosecha() {
        when(campanaRepository.findById(1L)).thenReturn(Optional.of(campanaEnCurso));
        when(cosechaRepository.save(any(Cosecha.class))).thenAnswer(inv -> inv.getArgument(0));
        CosechaRequestDTO dto = new CosechaRequestDTO(LocalDate.of(2026, 10, 1), 8000.0, "Cosecha final", null);

        Cosecha cosecha = campanaService.finalizarCampana(1L, dto);

        assertThat(cosecha.getCantidadKg()).isEqualTo(8000.0);
        assertThat(cosecha.getCampana()).isSameAs(campanaEnCurso);
        assertThat(campanaEnCurso.getEstado()).isEqualTo(EstadoCampana.FINALIZADA);
        verify(cosechaRepository).save(any(Cosecha.class));
        verify(campanaRepository).save(campanaEnCurso);
    }

    @Test
    @DisplayName("No debe guardar nada si la campana ya estaba finalizada (rollback)")
    void noDebeFinalizarUnaCampanaYaFinalizada() {
        campanaEnCurso.setEstado(EstadoCampana.FINALIZADA);
        when(campanaRepository.findById(1L)).thenReturn(Optional.of(campanaEnCurso));
        CosechaRequestDTO dto = new CosechaRequestDTO(LocalDate.of(2026, 10, 1), 8000.0, null, null);

        assertThatThrownBy(() -> campanaService.finalizarCampana(1L, dto))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("finalizada");
        verify(cosechaRepository, never()).save(any(Cosecha.class));
        verify(campanaRepository, never()).save(any(CampanaAgricola.class));
    }

    @Test
    @DisplayName("Debe rechazar una cosecha con cantidad no positiva")
    void debeRechazarCantidadInvalida() {
        CosechaRequestDTO dto = new CosechaRequestDTO(LocalDate.of(2026, 10, 1), 0.0, null, null);

        assertThatThrownBy(() -> campanaService.finalizarCampana(1L, dto))
                .isInstanceOf(InvalidDataException.class);
        verifyNoInteractions(cosechaRepository);
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la campana a finalizar no existe")
    void debeFallarSiLaCampanaNoExiste() {
        when(campanaRepository.findById(99L)).thenReturn(Optional.empty());
        CosechaRequestDTO dto = new CosechaRequestDTO(LocalDate.of(2026, 10, 1), 100.0, null, null);

        assertThatThrownBy(() -> campanaService.finalizarCampana(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Debe rechazar un rango de fechas invertido")
    void debeRechazarRangoDeFechasInvertido() {
        assertThatThrownBy(() -> campanaService.listarPorRangoDeFechas(
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(InvalidDataException.class);
        verifyNoInteractions(campanaRepository);
    }

    @Test
    @DisplayName("Debe delegar el filtro por estado al repositorio JPQL")
    void debeListarPorEstado() {
        when(campanaRepository.buscarPorEstado(EstadoCampana.EN_CURSO)).thenReturn(List.of(campanaEnCurso));

        List<CampanaAgricola> resultado = campanaService.listarPorEstado(EstadoCampana.EN_CURSO);

        assertThat(resultado).containsExactly(campanaEnCurso);
    }

    @Test
    @DisplayName("El resumen por estado debe incluir todos los estados, aunque no tengan campanas")
    void resumenDebeIncluirTodosLosEstados() {
        List<Object[]> filas = new java.util.ArrayList<>();
        filas.add(new Object[]{EstadoCampana.EN_CURSO, 2L});
        when(campanaRepository.contarPorEstado()).thenReturn(filas);

        Map<String, Long> resumen = campanaService.resumenPorEstado();

        assertThat(resumen).containsEntry("EN_CURSO", 2L)
                .containsEntry("PLANIFICADA", 0L)
                .containsEntry("FINALIZADA", 0L);
    }
}
