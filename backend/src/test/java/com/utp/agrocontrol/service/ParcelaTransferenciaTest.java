package com.utp.agrocontrol.service;

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

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas de la operacion transaccional transferir() y de las consultas de ParcelaService (APF2). */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de transferencia de parcelas y consultas (APF2)")
class ParcelaTransferenciaTest {

    @Mock private ParcelaRepository parcelaRepository;
    @Mock private ProductorRepository productorRepository;

    @InjectMocks
    private ParcelaServiceImpl parcelaService;

    private Productor julio;
    private Productor rosa;
    private Parcela parcela;

    @BeforeEach
    void setUp() {
        julio = new Productor(1L, "Julio", "Chunga", "41234567", null, null, new ArrayList<>());
        rosa = new Productor(2L, "Rosa", "Farfan", "45678912", null, null, new ArrayList<>());
        parcela = new Parcela(1L, "Parcela El Progreso", "Piura", 3.5, julio, new ArrayList<>());
    }

    @Test
    @DisplayName("Debe transferir la parcela al nuevo productor")
    void debeTransferirParcela() {
        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(productorRepository.findById(2L)).thenReturn(Optional.of(rosa));
        when(parcelaRepository.save(any(Parcela.class))).thenAnswer(inv -> inv.getArgument(0));

        Parcela resultado = parcelaService.transferir(1L, 2L);

        assertThat(resultado.getProductor()).isSameAs(rosa);
        verify(parcelaRepository).save(parcela);
    }

    @Test
    @DisplayName("No debe transferir a un productor inexistente")
    void noDebeTransferirAProductorInexistente() {
        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(productorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parcelaService.transferir(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(parcelaRepository, never()).save(any(Parcela.class));
    }

    @Test
    @DisplayName("No debe transferir la parcela al mismo productor que ya la posee")
    void noDebeTransferirAlMismoProductor() {
        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(productorRepository.findById(1L)).thenReturn(Optional.of(julio));

        assertThatThrownBy(() -> parcelaService.transferir(1L, 1L))
                .isInstanceOf(InvalidDataException.class);
        verify(parcelaRepository, never()).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Debe rechazar hectareas negativas en la consulta por area minima")
    void debeRechazarAreaMinimaNegativa() {
        assertThatThrownBy(() -> parcelaService.listarPorAreaMinima(-1.0))
                .isInstanceOf(InvalidDataException.class);
        verifyNoInteractions(parcelaRepository);
    }

    @Test
    @DisplayName("Debe calcular el area total solo si el productor existe")
    void debeCalcularAreaTotal() {
        when(productorRepository.existsById(1L)).thenReturn(true);
        when(parcelaRepository.calcularAreaTotalPorProductor(1L)).thenReturn(7.5);

        assertThat(parcelaService.calcularAreaTotalPorProductor(1L)).isEqualTo(7.5);
    }
}
