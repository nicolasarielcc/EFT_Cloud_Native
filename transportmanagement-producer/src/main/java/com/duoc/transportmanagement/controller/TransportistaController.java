package com.duoc.transportmanagement.controller;

import com.duoc.transportmanagement.dto.TransportistaDTO;
import com.duoc.transportmanagement.dto.TransportistaResumenDTO;
import com.duoc.transportmanagement.service.TransportistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
public class TransportistaController {

    @Autowired
    private TransportistaService transportistaService;

    // GET - Todos los transportistas
    @GetMapping
    public ResponseEntity<List<TransportistaResumenDTO>> findAll() {

        return ResponseEntity.ok(
                transportistaService.findAll()
        );
    }

    // GET - Transportista por ID
    @GetMapping("/{id}")
    public ResponseEntity<TransportistaResumenDTO> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                transportistaService.findById(id)
        );
    }

    // POST - Crear transportista
    @PostMapping
    public ResponseEntity<String> saveTransportista(
            @RequestBody TransportistaDTO dto) {

        return ResponseEntity.ok(
                        transportistaService.saveTransportista(dto)
                );
    }

    // PUT - Modificar transportista
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTransportista(
            @PathVariable Long id,
            @RequestBody TransportistaDTO dto) {

        return ResponseEntity.ok(
                transportistaService.updateTransportista(id, dto)
        );
    }

    // DELETE - Eliminar transportista
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransportista(
            @PathVariable Long id) {

        return ResponseEntity.ok(transportistaService.deleteTransportista(id));
    }
}
