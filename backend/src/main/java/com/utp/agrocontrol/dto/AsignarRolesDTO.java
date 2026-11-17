package com.utp.agrocontrol.dto;

import com.utp.agrocontrol.model.NombreRol;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/** Body de PUT /api/usuarios/{id}/roles (solo ADMIN). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarRolesDTO {

    @NotEmpty(message = "Debe indicar al menos un rol")
    private Set<NombreRol> roles;
}
