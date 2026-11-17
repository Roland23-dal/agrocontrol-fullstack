package com.utp.agrocontrol.repository;

import com.utp.agrocontrol.model.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
}
