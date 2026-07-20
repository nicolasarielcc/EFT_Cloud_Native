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

}
