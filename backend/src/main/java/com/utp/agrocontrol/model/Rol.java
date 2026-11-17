package com.utp.agrocontrol.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Rol de seguridad asignable a un Usuario.
 * Relacion: Muchos Usuarios pueden tener muchos Roles (N : M) -> tabla usuarios_roles.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NombreRol nombre;

    public Rol(NombreRol nombre) {
        this.nombre = nombre;
    }
}
