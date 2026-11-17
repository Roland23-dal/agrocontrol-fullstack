package com.utp.agrocontrol.service;

import com.utp.agrocontrol.dto.ProductorRequestDTO;
import com.utp.agrocontrol.exception.InvalidDataException;
import com.utp.agrocontrol.exception.ResourceNotFoundException;
import com.utp.agrocontrol.model.Productor;
import com.utp.agrocontrol.repository.ProductorRepository;
import com.utp.agrocontrol.service.impl.ProductorServiceImpl;
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
 * Pruebas unitarias de ProductorServiceImpl aplicando TDD.
 *
 * Documentacion de TDD (ver tambien README.md):
 * 1) Primero se escribio la prueba "debeRechazarProductorConDniInvalido",
 *    con ella se definio el contrato: el servicio debe lanzar InvalidDataException
 *    cuando el DNI no tiene 8 digitos, ANTES de existir la validacion en el
 *    codigo de produccion (la prueba fallaba en rojo).
 * 2) Luego se implemento el metodo validarDatos() en ProductorServiceImpl
 *    hasta que la prueba paso (verde).
 * 3) Esto influyo en el diseno: se decidio validar en la capa de servicio
 *    (no solo en el controlador) para que la regla de negocio sea reutilizable
 *    y facil de probar de forma aislada con Mockito, sin levantar el contexto
 *    completo de Spring ni la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del servicio ProductorService (TDD)")
class ProductorServiceTest {

    @Mock
    private ProductorRepository productorRepository;

    @InjectMocks
    private ProductorServiceImpl productorService;

    private ProductorRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new ProductorRequestDTO(
                "Julio", "Chunga Nima", "41234567", "969111222", "julio@agro.pe");
    }

    @Test
    @DisplayName("Debe registrar un productor cuando los datos son validos")
    void debeRegistrarProductorConDatosValidos() {
        when(productorRepository.existsByDni("41234567")).thenReturn(false);
        when(productorRepository.save(any(Productor.class))).thenAnswer(invocation -> {
            Productor p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Productor resultado = productorService.registrar(dtoValido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDni()).isEqualTo("41234567");
        verify(productorRepository, times(1)).save(any(Productor.class));
    }

    @Test
    @DisplayName("Debe rechazar el registro cuando el DNI no tiene 8 digitos")
    void debeRechazarProductorConDniInvalido() {
        ProductorRequestDTO dtoInvalido = new ProductorRequestDTO(
                "Julio", "Chunga Nima", "123", "969111222", "julio@agro.pe");

        assertThatThrownBy(() -> productorService.registrar(dtoInvalido))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("DNI");

        verify(productorRepository, never()).save(any(Productor.class));
    }

    @Test
    @DisplayName("Debe rechazar el registro cuando el nombre esta vacio")
    void debeRechazarProductorSinNombre() {
        ProductorRequestDTO dtoInvalido = new ProductorRequestDTO(
                "", "Chunga Nima", "41234567", "969111222", "julio@agro.pe");

        assertThatThrownBy(() -> productorService.registrar(dtoInvalido))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("nombre");

        verify(productorRepository, never()).save(any(Productor.class));
    }

    @Test
    @DisplayName("Debe rechazar el registro cuando el DNI ya existe")
    void debeRechazarProductorConDniDuplicado() {
        when(productorRepository.existsByDni("41234567")).thenReturn(true);

        assertThatThrownBy(() -> productorService.registrar(dtoValido))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Ya existe");

        verify(productorRepository, never()).save(any(Productor.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion al buscar un productor que no existe")
    void debeLanzarExcepcionSiProductorNoExiste() {
        when(productorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productorService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Debe listar todos los productores registrados")
    void debeListarTodosLosProductores() {
        Productor p1 = new Productor(1L, "Julio", "Chunga", "41234567", "969111222", "julio@agro.pe", List.of());
        Productor p2 = new Productor(2L, "Rosa", "Farfan", "45678912", "969333444", "rosa@agro.pe", List.of());
        when(productorRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Productor> resultado = productorService.listarTodos();

        assertThat(resultado).hasSize(2);
        verify(productorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe eliminar un productor existente")
    void debeEliminarProductorExistente() {
        Productor p1 = new Productor(1L, "Julio", "Chunga", "41234567", "969111222", "julio@agro.pe", List.of());
        when(productorRepository.findById(1L)).thenReturn(Optional.of(p1));

        productorService.eliminar(1L);

        verify(productorRepository, times(1)).delete(p1);
    }
}
