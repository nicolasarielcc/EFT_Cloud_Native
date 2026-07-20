package com.duoc.transportmanagement.controller;

import com.duoc.transportmanagement.dto.*;
import com.duoc.transportmanagement.service.GuiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    @Autowired
    private GuiaService guiaService;

    // GET - Todas las guías
    @GetMapping
    public ResponseEntity<List<GuiaResumenDTO>> findAll() {

        return ResponseEntity.ok(
                guiaService.findAll()
        );
    }

    // GET - Guía por ID
    @GetMapping("/{id}")
    public ResponseEntity<GuiaDTO> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                guiaService.findById(id)
        );
    }

    // POST - Crear guía
    @PostMapping
    public ResponseEntity<String> createGuia(
            @RequestBody GuiaCreateDTO dto) {

        return ResponseEntity.status(201)
                .body(
                        guiaService.createGuia(dto)
                );
    }

    // PUT - Actualizar guía
    @PutMapping("/{id}")
    public ResponseEntity<String> updateGuia(
            @PathVariable Long id,
            @RequestBody GuiaUpdateDTO dto) {

        return ResponseEntity.ok(
                guiaService.updateGuia(id, dto)
        );
    }

    // DELETE - Eliminar guía
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGuia(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                guiaService.deleteGuia(id)
        );
    }

    // GET - Guías por transportista
    @GetMapping("/transportista/{id}")
    public ResponseEntity<List<GuiaResumenDTO>> findByTransportista(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                guiaService.findByTransportista(id)
        );
    }

    // GET - Guías por fecha
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<GuiaResumenDTO>> findByFecha(
            @PathVariable LocalDate fecha) {

        return ResponseEntity.ok(
                guiaService.findByFecha(fecha)
        );
    }

    // POST - Subir a S3
    @PostMapping("/s3/{id}")
    public ResponseEntity<String> subirArchivoS3(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                guiaService.subirArchivoS3(id)
        );
    }

    // PUT - Actualizar archivo en S3
    @PutMapping("/s3/{id}")
    public ResponseEntity<String> actualizarArchivoS3(
            @PathVariable Long id,
            @RequestBody GuiaUpdateDTO dto) {

        return ResponseEntity.ok(
                guiaService.actualizarArchivoS3(id, dto)
        );
    }

    // GET - Descargar archivo
    @GetMapping("/s3/{id}")
    public ResponseEntity<byte[]> descargarArchivo(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                guiaService.descargarArchivo(id)
        );
    }

    // DELETE - Eliminar archivo S3
    @DeleteMapping("/s3/{id}")
    public ResponseEntity<String> eliminarArchivoS3(
            @PathVariable Long id) {

        guiaService.eliminarArchivoS3(id);

        return ResponseEntity.noContent().build();
    }
}
