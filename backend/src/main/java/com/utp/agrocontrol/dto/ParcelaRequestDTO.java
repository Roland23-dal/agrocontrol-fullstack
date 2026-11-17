package com.utp.agrocontrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para crear o actualizar una Parcela.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelaRequestDTO {

    @NotBlank(message = "El nombre de la parcela es obligatorio")
    private String nombre;

    private String ubicacion;

    @NotNull(message = "El area en hectareas es obligatoria")
    @Positive(message = "El area en hectareas debe ser un valor positivo")
    private Double areaHectareas;

    @NotNull(message = "Debe indicar el id del productor propietario")
    private Long productorId;
}
