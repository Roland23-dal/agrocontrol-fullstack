package com.utp.agrocontrol.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Registro de una cosecha (produccion obtenida) de una Campana Agricola.
 * Relacion: Muchas Cosechas pertenecen a una Campana (N : 1).
 */
@Entity
@Table(name = "cosechas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Cosecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_kg", nullable = false)
    private Double cantidadKg;

    @Column(length = 255)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campana_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CampanaAgricola campana;
}
