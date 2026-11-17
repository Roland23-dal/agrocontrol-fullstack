package com.utp.agrocontrol.repository;

import com.utp.agrocontrol.model.NombreRol;
import com.utp.agrocontrol.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(NombreRol nombre);
}
