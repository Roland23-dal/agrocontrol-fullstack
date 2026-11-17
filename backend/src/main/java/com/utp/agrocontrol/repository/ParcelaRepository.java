package com.utp.agrocontrol.repository;

import com.utp.agrocontrol.model.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

    List<Parcela> findByProductorId(Long productorId);

    // ---------------- Consultas personalizadas con JPQL (APF2) ----------------

    /** Parcelas con un area (en hectareas) igual o mayor a la indicada. */
    @Query("SELECT p FROM Parcela p JOIN FETCH p.productor " +
           "WHERE p.areaHectareas >= :area ORDER BY p.areaHectareas DESC")
    List<Parcela> buscarPorAreaMinima(@Param("area") Double area);

    /** Parcelas cuya ubicacion contiene el texto indicado. */
    @Query("SELECT p FROM Parcela p JOIN FETCH p.productor " +
           "WHERE LOWER(p.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%')) " +
           "ORDER BY p.nombre")
    List<Parcela> buscarPorUbicacion(@Param("ubicacion") String ubicacion);

    /** Suma de hectareas de todas las parcelas de un productor. */
    @Query("SELECT COALESCE(SUM(p.areaHectareas), 0.0) FROM Parcela p WHERE p.productor.id = :productorId")
    Double calcularAreaTotalPorProductor(@Param("productorId") Long productorId);
}
