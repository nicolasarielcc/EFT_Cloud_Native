package com.duoc.cursosonline.controller;

import com.duoc.cursosonline.dto.CursoMessageDTO;
import com.duoc.cursosonline.dto.InscripcionMessageDTO;
import com.duoc.cursosonline.listener.CursoConsumer;
import com.duoc.cursosonline.listener.InscripcionConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/rabbit")
public class RabbitMQController {

    @Autowired
    private InscripcionConsumer inscripcionConsumer;

    @Autowired
    private CursoConsumer cursoConsumer;

    @PostMapping("/inscripcion/procesar")
    public ResponseEntity<String> procesarQueueInscripcion(InscripcionMessageDTO dto) {

        inscripcionConsumer.receive(dto);

        return ResponseEntity.ok("Inscripcion procesada correctamente.");
    }

    @PostMapping("/curso/procesar")
    public ResponseEntity<String> procesarQueueCurso(CursoMessageDTO dto) {

        cursoConsumer.receive(dto);

        return ResponseEntity.ok("Curso procesado correctamente.");
    }

}
