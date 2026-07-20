package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.CursoMessageDTO;
import com.duoc.cursosonline.util.RabbitConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQCursoProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(CursoMessageDTO message){
        if(message.getCursoDTO() != null){
            System.out.println("Nombre: " + message.getCursoDTO().getNombre());
        }

        try{
            rabbitTemplate.convertAndSend(
                    RabbitConstants.CURSO_QUEUE,
                    message
            );
        }catch (Exception e){
            throw new RuntimeException("Error al enviar el curso a RabbitMQ", e);
        }
    }
}
