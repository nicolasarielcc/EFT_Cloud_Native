package com.duoc.cursosonline.repository;

import com.duoc.cursosonline.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
