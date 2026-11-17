package com.utp.agrocontrol.repository;

import com.utp.agrocontrol.model.Cosecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CosechaRepository extends JpaRepository<Cosecha, Long> {

    List<Cosecha> findByCampanaId(Long campanaId);

    // ---------------- Consultas personalizadas con JPQL (APF2) ----------------

    /** Cosechas realizadas dentro de un rango de fechas. */
    @Query("SELECT c FROM Cosecha c JOIN FETCH c.campana " +
           "WHERE c.fecha BETWEEN :desde AND :hasta ORDER BY c.fecha")
    List<Cosecha> buscarPorRangoDeFechas(@Param("desde") LocalDate desde,
                                         @Param("hasta") LocalDate hasta);

    /** Total de kilogramos cosechados en una campana. */
    @Query("SELECT COALESCE(SUM(c.cantidadKg), 0.0) FROM Cosecha c WHERE c.campana.id = :campanaId")
    Double calcularTotalKgPorCampana(@Param("campanaId") Long campanaId);
}
