package com.duoc.transportmanagement.listener;

import com.duoc.transportmanagement.dto.GuiaMessageDTO;
import com.duoc.transportmanagement.repository.S3Repository;
import com.duoc.transportmanagement.service.GuiaService;
import com.duoc.transportmanagement.util.RabbitConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GuiaConsumer {

    @Autowired
    private GuiaService guiaService;

    @RabbitListener(
            queues = RabbitConstants.GUIA_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(GuiaMessageDTO dto){
        try {
            switch (dto.getOperacion()) {

                case "CREATE":
                    guiaService.createGuia(dto.getGuiaCreate());
                    break;

                case "UPDATE":
                    guiaService.updateGuia(dto.getId(), dto.getGuiaUpdate());
                    break;

                case "DELETE":
                    guiaService.deleteGuia(dto.getId());
                    break;

                case "UPLOAD_S3":
                    guiaService.subirArchivoS3(dto.getId());
                    break;

                case "UPDATE_S3":
                    guiaService.actualizarArchivoS3(dto.getId(), dto.getGuiaUpdate());
                    break;

                case "DELETE_S3":
                    guiaService.eliminarArchivoS3(dto.getId());
                    break;
                default:
                    throw new IllegalArgumentException("Operación no válida: " + dto.getOperacion());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
