package com.duoc.transportmanagement.controller;

import com.duoc.transportmanagement.dto.GuiaMessageDTO;
import com.duoc.transportmanagement.dto.TransportistaMessageDTO;
import com.duoc.transportmanagement.listener.GuiaConsumer;
import com.duoc.transportmanagement.listener.TransportistaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/rabbit")
public class RabbitMQController {

    @Autowired
    private GuiaConsumer guiaConsumer;

    @Autowired
    private TransportistaConsumer transportistaConsumer;

    @PostMapping("/guia/procesar")
    public ResponseEntity<String> procesarQueueGuia(GuiaMessageDTO dto) {

        guiaConsumer.receive(dto);

        return ResponseEntity.ok("Guia procesada correctamente.");
    }

    @PostMapping("/transportista/procesar")
    public ResponseEntity<String> procesarQueueTransportista(TransportistaMessageDTO dto) {

        transportistaConsumer.receive(dto);

        return ResponseEntity.ok("Transportista procesado correctamente.");
    }

}
