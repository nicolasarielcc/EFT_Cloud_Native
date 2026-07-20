package com.duoc.transportmanagement.listener;

import com.duoc.transportmanagement.dto.TransportistaMessageDTO;
import com.duoc.transportmanagement.service.TransportistaService;
import com.duoc.transportmanagement.util.RabbitConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransportistaConsumer {

    @Autowired
    private TransportistaService transportistaService;

    @RabbitListener(
            queues = RabbitConstants.TRANSPORTISTA_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(TransportistaMessageDTO dto){
        try {
            switch (dto.getOperacion()) {

                case "CREATE":
                    transportistaService.saveTransportista(dto.getTransportistaDTO());
                    break;

                case "UPDATE":
                    transportistaService.updateTransportista(dto.getId(),
                            dto.getTransportistaDTO());
                    break;

                case "DELETE":
                    transportistaService.deleteTransportista(dto.getId());
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Operación no soportada: " + dto.getOperacion());
            }
        }catch (Exception e){
            e.printStackTrace();

            throw e;
        }
    }
}
