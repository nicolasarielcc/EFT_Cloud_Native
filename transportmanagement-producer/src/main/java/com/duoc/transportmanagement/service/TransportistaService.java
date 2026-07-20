package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.TransportistaDTO;
import com.duoc.transportmanagement.dto.TransportistaMessageDTO;
import com.duoc.transportmanagement.dto.TransportistaResumenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportistaService {

    @Autowired
    private ConsumerClient consumerClient;

    private final RabbitMQTransportistaProducer producer;

    public TransportistaService(RabbitMQTransportistaProducer producer) {
        this.producer = producer;
    }

    public List<TransportistaResumenDTO> findAll() {
        return consumerClient.findAllTransportistas();
    }

    public TransportistaResumenDTO findById(Long id) {

        return consumerClient.findTransportista(id);
    }

    public String saveTransportista(TransportistaDTO dto) {

        TransportistaMessageDTO mensaje = new TransportistaMessageDTO();
        mensaje.setOperacion("CREATE");
        mensaje.setTransportistaDTO(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para crear un transportista enviada a RabbitMQ";
    }

    public String updateTransportista(Long id,
                                                       TransportistaDTO dto) {

        TransportistaMessageDTO mensaje =
                new TransportistaMessageDTO();

        mensaje.setOperacion("UPDATE");
        mensaje.setId(id);
        mensaje.setTransportistaDTO(dto);

        producer.sendMessage(mensaje);

        return "Solicitud para actualizar un transportista enviada a RabbitMQ";
    }

    public String deleteTransportista(Long id) {

        TransportistaMessageDTO mensaje =
                new TransportistaMessageDTO();

        mensaje.setOperacion("DELETE");
        mensaje.setId(id);

        producer.sendMessage(mensaje);

        return "Solicitud para eliminar un transportista enviada a RabbitMQ";
    }

}
