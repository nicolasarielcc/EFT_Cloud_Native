package com.duoc.cursosonline.controller;

import com.duoc.cursosonline.dto.*;
import com.duoc.cursosonline.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    // GET - Todas las inscripciones
    @GetMapping
    public ResponseEntity<List<InscripcionResumenDTO>> findAll() {

        return ResponseEntity.ok(
                inscripcionService.findAll()
        );
    }

    // GET - Inscripción por ID
    @GetMapping("/{id}")
    public ResponseEntity<InscripcionDTO> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inscripcionService.findById(id)
        );
    }

    // GET - Inscripciones por curso
    @GetMapping("/curso/{id}")
    public ResponseEntity<List<InscripcionResumenDTO>> findByCurso(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inscripcionService.findByCurso(id)
        );
    }

    // GET - Inscripciones por fecha
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<InscripcionResumenDTO>> findByFecha(
            @PathVariable LocalDate fecha) {

        return ResponseEntity.ok(
                inscripcionService.findByFecha(fecha)
        );
    }

    // GET - Descargar certificado
    @GetMapping("/certificado/{id}")
    public ResponseEntity<byte[]> descargarCertificado(
            @PathVariable Long id) {

        byte[] archivo = inscripcionService.descargarCertificado(id);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=certificado_" + id + ".txt"
                )
                .body(archivo);
    }
}
