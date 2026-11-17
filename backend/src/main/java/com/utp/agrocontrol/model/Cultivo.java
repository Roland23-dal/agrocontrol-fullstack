package com.utp.agrocontrol.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un tipo de cultivo que puede sembrarse en una parcela (catalogo).
 */
@Entity
@Table(name = "cultivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 80)
    private String variedad;

    @Column(name = "ciclo_dias_estimado")
    private Integer cicloDiasEstimado;
}
