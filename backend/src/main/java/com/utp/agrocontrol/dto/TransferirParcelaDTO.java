package com.utp.agrocontrol.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO para transferir una parcela a otro productor. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferirParcelaDTO {

    @NotNull(message = "Debe indicar el id del nuevo productor")
    private Long nuevoProductorId;
}
