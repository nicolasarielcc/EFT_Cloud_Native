package com.duoc.cursosonline.controller;

import com.duoc.cursosonline.dto.CursoDTO;
import com.duoc.cursosonline.dto.CursoResumenDTO;
import com.duoc.cursosonline.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    // GET - Todos los cursos
    @GetMapping
    public ResponseEntity<List<CursoResumenDTO>> findAll() {

        return ResponseEntity.ok(
                cursoService.findAll()
        );
    }

    // GET - Curso por ID
    @GetMapping("/{id}")
    public ResponseEntity<CursoResumenDTO> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                cursoService.findById(id)
        );
    }

    // POST - Crear curso (instructor)
    @PostMapping
    public ResponseEntity<String> saveCurso(
            @RequestBody CursoDTO dto) {

        return ResponseEntity.ok(
                        cursoService.saveCurso(dto)
                );
    }

    // PUT - Modificar curso (instructor)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCurso(
            @PathVariable Long id,
            @RequestBody CursoDTO dto) {

        return ResponseEntity.ok(
                cursoService.updateCurso(id, dto)
        );
    }

    // DELETE - Eliminar curso (instructor)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCurso(
            @PathVariable Long id) {

        return ResponseEntity.ok(cursoService.deleteCurso(id));
    }
}
