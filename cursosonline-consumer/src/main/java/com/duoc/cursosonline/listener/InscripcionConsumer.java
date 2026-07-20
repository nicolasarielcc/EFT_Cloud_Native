package com.duoc.cursosonline.listener;

import com.duoc.cursosonline.dto.InscripcionMessageDTO;
import com.duoc.cursosonline.service.InscripcionService;
import com.duoc.cursosonline.util.RabbitConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InscripcionConsumer {

    @Autowired
    private InscripcionService inscripcionService;

    @RabbitListener(
            queues = RabbitConstants.INSCRIPCION_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(InscripcionMessageDTO dto){
        try {
            switch (dto.getOperacion()) {

                case "CREATE":
                    inscripcionService.createInscripcion(dto.getInscripcionCreate());
                    break;

                case "UPDATE":
                    inscripcionService.updateInscripcion(dto.getId(), dto.getInscripcionUpdate());
                    break;

                case "DELETE":
                    inscripcionService.deleteInscripcion(dto.getId());
                    break;

                case "UPLOAD_S3":
                    inscripcionService.subirCertificadoS3(dto.getId());
                    break;

                case "UPDATE_S3":
                    inscripcionService.actualizarCertificadoS3(dto.getId(), dto.getInscripcionUpdate());
                    break;

                case "DELETE_S3":
                    inscripcionService.eliminarCertificadoS3(dto.getId());
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
