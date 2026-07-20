package com.duoc.cursosonline.repository;

import com.duoc.cursosonline.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByCursoId(Long id);

    List<Inscripcion> findByFechaInscripcion(LocalDate fecha);
}
