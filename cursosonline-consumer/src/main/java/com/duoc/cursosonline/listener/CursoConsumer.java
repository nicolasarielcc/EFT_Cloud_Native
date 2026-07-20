package com.duoc.cursosonline.listener;

import com.duoc.cursosonline.dto.CursoMessageDTO;
import com.duoc.cursosonline.service.CursoService;
import com.duoc.cursosonline.util.RabbitConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CursoConsumer {

    @Autowired
    private CursoService cursoService;

    @RabbitListener(
            queues = RabbitConstants.CURSO_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(CursoMessageDTO dto){
        try {
            switch (dto.getOperacion()) {

                case "CREATE":
                    cursoService.saveCurso(dto.getCursoDTO());
                    break;

                case "UPDATE":
                    cursoService.updateCurso(dto.getId(),
                            dto.getCursoDTO());
                    break;

                case "DELETE":
                    cursoService.deleteCurso(dto.getId());
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
