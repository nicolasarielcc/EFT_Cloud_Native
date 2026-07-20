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

    // POST - Inscribir estudiante a un curso
    @PostMapping
    public ResponseEntity<String> createInscripcion(
            @RequestBody InscripcionCreateDTO dto) {

        return ResponseEntity.status(201)
                .body(
                        inscripcionService.createInscripcion(dto)
                );
    }

    // PUT - Actualizar inscripción / registrar calificación (instructor, tiempo real)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateInscripcion(
            @PathVariable Long id,
            @RequestBody InscripcionUpdateDTO dto) {

        return ResponseEntity.ok(
                inscripcionService.updateInscripcion(id, dto)
        );
    }

    // DELETE - Anular inscripción
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInscripcion(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inscripcionService.deleteInscripcion(id)
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

    // POST - Generar y subir certificado a S3
    @PostMapping("/certificado/{id}")
    public ResponseEntity<String> subirCertificadoS3(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inscripcionService.subirCertificadoS3(id)
        );
    }

    // PUT - Actualizar certificado en S3
    @PutMapping("/certificado/{id}")
    public ResponseEntity<String> actualizarCertificadoS3(
            @PathVariable Long id,
            @RequestBody InscripcionUpdateDTO dto) {

        return ResponseEntity.ok(
                inscripcionService.actualizarCertificadoS3(id, dto)
        );
    }

    // GET - Descargar certificado desde S3
    @GetMapping("/certificado/{id}")
    public ResponseEntity<byte[]> descargarCertificado(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inscripcionService.descargarCertificado(id)
        );
    }

    // DELETE - Eliminar certificado de S3
    @DeleteMapping("/certificado/{id}")
    public ResponseEntity<String> eliminarCertificadoS3(
            @PathVariable Long id) {

        inscripcionService.eliminarCertificadoS3(id);

        return ResponseEntity.noContent().build();
    }
}
