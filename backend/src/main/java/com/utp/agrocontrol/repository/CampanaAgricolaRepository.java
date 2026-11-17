package com.utp.agrocontrol.repository;

import com.utp.agrocontrol.model.CampanaAgricola;
import com.utp.agrocontrol.model.EstadoCampana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CampanaAgricolaRepository extends JpaRepository<CampanaAgricola, Long> {

    List<CampanaAgricola> findByParcelaId(Long parcelaId);

    // ---------------- Consultas personalizadas con JPQL (APF2) ----------------

    /** Filtra campanas por estado (PLANIFICADA, EN_CURSO, FINALIZADA). */
    @Query("SELECT c FROM CampanaAgricola c " +
           "JOIN FETCH c.cultivo JOIN FETCH c.parcela " +
           "WHERE c.estado = :estado " +
           "ORDER BY c.fechaInicio DESC")
    List<CampanaAgricola> buscarPorEstado(@Param("estado") EstadoCampana estado);

    /** Filtra campanas cuya fecha de inicio esta dentro de un rango de fechas. */
    @Query("SELECT c FROM CampanaAgricola c " +
           "JOIN FETCH c.cultivo JOIN FETCH c.parcela " +
           "WHERE c.fechaInicio BETWEEN :desde AND :hasta " +
           "ORDER BY c.fechaInicio")
    List<CampanaAgricola> buscarPorRangoDeFechas(@Param("desde") LocalDate desde,
                                                 @Param("hasta") LocalDate hasta);

    /** Filtra campanas por categoria de cultivo (nombre parcial, sin distinguir mayusculas). */
    @Query("SELECT c FROM CampanaAgricola c " +
           "JOIN FETCH c.cultivo cu JOIN FETCH c.parcela " +
           "WHERE LOWER(cu.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "ORDER BY c.fechaInicio DESC")
    List<CampanaAgricola> buscarPorNombreCultivo(@Param("nombre") String nombre);

    /** Campanas en un estado dado cuya fecha fin estimada ya vencio (campanas atrasadas). */
    @Query("SELECT c FROM CampanaAgricola c " +
           "JOIN FETCH c.cultivo JOIN FETCH c.parcela " +
           "WHERE c.estado = :estado AND c.fechaFinEstimada < :fecha " +
           "ORDER BY c.fechaFinEstimada")
    List<CampanaAgricola> buscarAtrasadas(@Param("estado") EstadoCampana estado,
                                          @Param("fecha") LocalDate fecha);

    /** Cantidad de campanas agrupadas por estado (para reportes / dashboard). */
    @Query("SELECT c.estado, COUNT(c) FROM CampanaAgricola c GROUP BY c.estado")
    List<Object[]> contarPorEstado();
}
