package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.GuiaMessageDTO;
import com.duoc.transportmanagement.util.RabbitConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQGuiaProducer {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(GuiaMessageDTO message){
        try{
            rabbitTemplate.convertAndSend(
                    RabbitConstants.GUIA_QUEUE,
                    message
            );
        }catch (Exception e){
            throw new RuntimeException("Error al enviar la guia RabbitMQ", e);
        }
    }
}
