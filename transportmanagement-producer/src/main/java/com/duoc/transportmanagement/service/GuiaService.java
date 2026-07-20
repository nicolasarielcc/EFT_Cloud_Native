package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GuiaService {

    @Autowired
    private ConsumerClient consumerClient;

    private final RabbitMQGuiaProducer producer;

    public GuiaService(RabbitMQGuiaProducer producer) {
        this.producer = producer;
    }

    public List<GuiaResumenDTO> findAll() {
        return consumerClient.findAllGuias();
    }

    public GuiaDTO findById(Long id) {
        return consumerClient.findGuiaById(id);
    }

    public List<GuiaResumenDTO> findByFecha(LocalDate fecha) {
        return consumerClient.findByFecha(fecha);
    }

    public String createGuia(GuiaCreateDTO dto) {

        GuiaMessageDTO mensaje = new GuiaMessageDTO();
        mensaje.setOperacion("CREATE");
        mensaje.setGuiaCreate(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para crear la guia enviada a RabbitMQ";
    }

    public String updateGuia(Long id, GuiaUpdateDTO dto) {

        GuiaMessageDTO mensaje =
                new GuiaMessageDTO();

        mensaje.setOperacion("UPDATE");
        mensaje.setId(id);
        mensaje.setGuiaUpdate(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para actualizar la guia enviada a RabbitMQ";
    }

    public String deleteGuia(Long id) {

        GuiaMessageDTO mensaje =
                new GuiaMessageDTO();

        mensaje.setOperacion("DELETE");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para eliminar la guia enviada a RabbitMQ";
    }

    public List<GuiaResumenDTO> findByTransportista(Long transportistaId){

        return consumerClient.findByTransportista(transportistaId);
    }

    public String subirArchivoS3(Long id){

        GuiaMessageDTO mensaje =
                new GuiaMessageDTO();

        mensaje.setOperacion("UPLOAD_S3");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para subir archivo a S3 enviada a RabbitMQ";
    }

    public String actualizarArchivoS3(Long id, GuiaUpdateDTO dto){


        GuiaMessageDTO mensaje =
                new GuiaMessageDTO();

        mensaje.setOperacion("UPDATE_S3");
        mensaje.setId(id);
        mensaje.setGuiaUpdate(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para actualizar archivo en S3 enviada a RabbitMQ";
    }

    public byte[] descargarArchivo(Long id){
        return consumerClient.descargarArchivo(id);
    }

    public String eliminarArchivoS3(Long id){

        GuiaMessageDTO mensaje =
                new GuiaMessageDTO();

        mensaje.setOperacion("DELETE_S3");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para eliminar archivo en S3 enviada a RabbitMQ";
    }
}
