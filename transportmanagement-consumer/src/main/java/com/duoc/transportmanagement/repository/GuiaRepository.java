package com.duoc.transportmanagement.repository;

import com.duoc.transportmanagement.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface GuiaRepository extends JpaRepository<GuiaDespacho, Long> {

    List<GuiaDespacho> findByTransportistaId(Long id);

    List<GuiaDespacho> findByFechaGeneracion(LocalDate fecha);
}
