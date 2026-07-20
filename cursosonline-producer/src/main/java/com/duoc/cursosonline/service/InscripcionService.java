package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private ConsumerClient consumerClient;

    private final RabbitMQInscripcionProducer producer;

    public InscripcionService(RabbitMQInscripcionProducer producer) {
        this.producer = producer;
    }

    public List<InscripcionResumenDTO> findAll() {
        return consumerClient.findAllInscripciones();
    }

    public InscripcionDTO findById(Long id) {
        return consumerClient.findInscripcionById(id);
    }

    public List<InscripcionResumenDTO> findByFecha(LocalDate fecha) {
        return consumerClient.findByFecha(fecha);
    }

    public String createInscripcion(InscripcionCreateDTO dto) {

        InscripcionMessageDTO mensaje = new InscripcionMessageDTO();
        mensaje.setOperacion("CREATE");
        mensaje.setInscripcionCreate(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para crear la inscripcion enviada a RabbitMQ";
    }

    public String updateInscripcion(Long id, InscripcionUpdateDTO dto) {

        InscripcionMessageDTO mensaje =
                new InscripcionMessageDTO();

        mensaje.setOperacion("UPDATE");
        mensaje.setId(id);
        mensaje.setInscripcionUpdate(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para actualizar la inscripcion enviada a RabbitMQ";
    }

    public String deleteInscripcion(Long id) {

        InscripcionMessageDTO mensaje =
                new InscripcionMessageDTO();

        mensaje.setOperacion("DELETE");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para eliminar la inscripcion enviada a RabbitMQ";
    }

    public List<InscripcionResumenDTO> findByCurso(Long cursoId){

        return consumerClient.findByCurso(cursoId);
    }

    public String subirCertificadoS3(Long id){

        InscripcionMessageDTO mensaje =
                new InscripcionMessageDTO();

        mensaje.setOperacion("UPLOAD_S3");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para subir certificado a S3 enviada a RabbitMQ";
    }

    public String actualizarCertificadoS3(Long id, InscripcionUpdateDTO dto){


        InscripcionMessageDTO mensaje =
                new InscripcionMessageDTO();

        mensaje.setOperacion("UPDATE_S3");
        mensaje.setId(id);
        mensaje.setInscripcionUpdate(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para actualizar certificado en S3 enviada a RabbitMQ";
    }

    public byte[] descargarCertificado(Long id){
        return consumerClient.descargarCertificado(id);
    }

    public String eliminarCertificadoS3(Long id){

        InscripcionMessageDTO mensaje =
                new InscripcionMessageDTO();

        mensaje.setOperacion("DELETE_S3");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para eliminar certificado en S3 enviada a RabbitMQ";
    }
}
