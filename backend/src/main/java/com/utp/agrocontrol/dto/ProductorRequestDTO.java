package com.utp.agrocontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para crear o actualizar un Productor.
 * Contiene las validaciones de entrada (Bean Validation) que se ejecutan
 * automaticamente antes de llegar al controlador gracias a @Valid.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductorRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos numericos")
    private String dni;

    private String telefono;

    @Email(message = "El email no tiene un formato valido")
    private String email;
}
