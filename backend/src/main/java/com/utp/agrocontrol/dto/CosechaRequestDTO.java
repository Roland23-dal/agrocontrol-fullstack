package com.utp.agrocontrol.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para registrar o actualizar una cosecha.
 * El campo campanaId es obligatorio al crear una cosecha (POST /api/cosechas);
 * no se usa en POST /api/campanas/{id}/finalizar porque la campana viene en la URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CosechaRequestDTO {

    @NotNull(message = "La fecha de la cosecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La cantidad en kilogramos es obligatoria")
    @Positive(message = "La cantidad en kilogramos debe ser un valor positivo")
    private Double cantidadKg;

    private String observacion;

    private Long campanaId;
}
