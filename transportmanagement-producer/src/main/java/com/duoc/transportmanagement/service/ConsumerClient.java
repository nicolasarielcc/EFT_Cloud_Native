package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.GuiaDTO;
import com.duoc.transportmanagement.dto.GuiaResumenDTO;
import com.duoc.transportmanagement.dto.TransportistaDTO;
import com.duoc.transportmanagement.dto.TransportistaResumenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Component
public class ConsumerClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${aws.consumer.url}")
    private String consumerUrl;

    // GUIAS

    public GuiaDTO findGuiaById(Long id) {

        return restTemplate.getForObject(
                consumerUrl + "/api/guias/" + id,
                GuiaDTO.class
        );
    }

    public List<GuiaResumenDTO> findAllGuias() {

        ResponseEntity<List<GuiaResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/guias",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<GuiaResumenDTO>>() {
                        });

        return response.getBody();
    }

    public List<GuiaResumenDTO> findByTransportista(Long id) {

        ResponseEntity<List<GuiaResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/guias/transportista/" + id,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<GuiaResumenDTO>>() {
                        });

        return response.getBody();
    }

    public List<GuiaResumenDTO> findByFecha(LocalDate fecha) {

        ResponseEntity<List<GuiaResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/guias/fecha/" + fecha,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<GuiaResumenDTO>>() {
                        });

        return response.getBody();
    }

    public byte[] descargarArchivo(Long id) {
        return restTemplate.exchange(
                consumerUrl + "/api/guias/s3/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<byte[]>() {
                }
        ).getBody();
    }

    // TRANSPORTISTAS

    public List<TransportistaResumenDTO> findAllTransportistas() {

        ResponseEntity<List<TransportistaResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/transportistas",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<TransportistaResumenDTO>>() {
                        });

        return response.getBody();
    }

    public TransportistaResumenDTO findTransportista(Long id) {

        return restTemplate.getForObject(
                consumerUrl + "/api/transportistas/" + id,
                TransportistaResumenDTO.class
        );
    }

}
