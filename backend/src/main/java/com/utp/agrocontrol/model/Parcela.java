package com.utp.agrocontrol.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una parcela agricola perteneciente a un productor.
 * Relaciones:
 *  - Muchas Parcelas pertenecen a un Productor (N : 1).
 *  - Una Parcela puede tener muchas Campanas Agricolas a lo largo del tiempo (1 : N).
 */
@Entity
@Table(name = "parcelas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 150)
    private String ubicacion;

    @Column(name = "area_hectareas")
    private Double areaHectareas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productor_id", nullable = false)
    private Productor productor;

    @OneToMany(mappedBy = "parcela", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CampanaAgricola> campanas = new ArrayList<>();
}
