package com.utp.agrocontrol.repository;

import com.utp.agrocontrol.model.Productor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductorRepository extends JpaRepository<Productor, Long> {
    Optional<Productor> findByDni(String dni);
    boolean existsByDni(String dni);

    // ---------------- Consultas personalizadas con JPQL (APF2) ----------------

    /** Busca productores por coincidencia parcial en nombres o apellidos. */
    @Query("SELECT p FROM Productor p " +
           "WHERE LOWER(p.nombres) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "   OR LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "ORDER BY p.apellidos, p.nombres")
    List<Productor> buscarPorNombreOApellido(@Param("texto") String texto);

    /** Productores que poseen al menos una parcela con mas hectareas que el valor indicado. */
    @Query("SELECT DISTINCT pr FROM Productor pr JOIN pr.parcelas pa " +
           "WHERE pa.areaHectareas > :area ORDER BY pr.apellidos")
    List<Productor> buscarConParcelasMayoresA(@Param("area") Double area);
}
