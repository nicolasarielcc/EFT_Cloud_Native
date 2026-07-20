package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.CursoDTO;
import com.duoc.cursosonline.dto.CursoMessageDTO;
import com.duoc.cursosonline.dto.CursoResumenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {

    @Autowired
    private ConsumerClient consumerClient;

    private final RabbitMQCursoProducer producer;

    public CursoService(RabbitMQCursoProducer producer) {
        this.producer = producer;
    }

    public List<CursoResumenDTO> findAll() {
        return consumerClient.findAllCursos();
    }

    public CursoResumenDTO findById(Long id) {

        return consumerClient.findCurso(id);
    }

    public String saveCurso(CursoDTO dto) {

        CursoMessageDTO mensaje = new CursoMessageDTO();
        mensaje.setOperacion("CREATE");
        mensaje.setCursoDTO(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para crear un curso enviada a RabbitMQ";
    }

    public String updateCurso(Long id,
                              CursoDTO dto) {

        CursoMessageDTO mensaje =
                new CursoMessageDTO();

        mensaje.setOperacion("UPDATE");
        mensaje.setId(id);
        mensaje.setCursoDTO(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para actualizar un curso enviada a RabbitMQ";
    }

    public String deleteCurso(Long id) {

        CursoMessageDTO mensaje =
                new CursoMessageDTO();

        mensaje.setOperacion("DELETE");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para eliminar un curso enviada a RabbitMQ";
    }

}
