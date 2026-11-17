package com.utp.agrocontrol.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una campana agricola: el proceso productivo de un Cultivo especifico
 * dentro de una Parcela especifica, durante un periodo de tiempo determinado.
 * Relaciones: Muchas Campanas pertenecen a una Parcela (N:1) y a un Cultivo (N:1);
 * una Campana puede tener muchas Cosechas (1:N).
 */
@Entity
@Table(name = "campanas_agricolas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CampanaAgricola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCampana estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    private Parcela parcela;

    @OneToMany(mappedBy = "campana", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Cosecha> cosechas = new ArrayList<>();
}
