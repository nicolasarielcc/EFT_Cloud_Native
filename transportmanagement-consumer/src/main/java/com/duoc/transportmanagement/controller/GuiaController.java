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

    // GET - Descargar archivo
    @GetMapping("/s3/{id}")
    public ResponseEntity<byte[]> descargarArchivo(
            @PathVariable Long id) {

        byte[] archivo = guiaService.descargarArchivo(id);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=guia_" + id + ".txt"
                )
                .body(archivo);
    }
}
