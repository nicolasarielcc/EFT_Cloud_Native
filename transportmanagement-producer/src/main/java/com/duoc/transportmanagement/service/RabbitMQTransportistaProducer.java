package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.TransportistaMessageDTO;
import com.duoc.transportmanagement.util.RabbitConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQTransportistaProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(TransportistaMessageDTO message){
        if(message.getTransportistaDTO() != null){
            System.out.println("Nombre: " + message.getTransportistaDTO().getNombre());
        }

        try{
            rabbitTemplate.convertAndSend(
                    RabbitConstants.TRANSPORTISTA_QUEUE,
                    message
            );
        }catch (Exception e){
            throw new RuntimeException("Error al enviar el transportista RabbitMQ", e);
        }
    }
}
