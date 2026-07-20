package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.InscripcionMessageDTO;
import com.duoc.cursosonline.util.RabbitConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQInscripcionProducer {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(InscripcionMessageDTO message){
        try{
            rabbitTemplate.convertAndSend(
                    RabbitConstants.INSCRIPCION_QUEUE,
                    message
            );
        }catch (Exception e){
            throw new RuntimeException("Error al enviar la inscripcion a RabbitMQ", e);
        }
    }
}
