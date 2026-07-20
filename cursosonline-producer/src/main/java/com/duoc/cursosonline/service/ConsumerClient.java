package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.CursoResumenDTO;
import com.duoc.cursosonline.dto.InscripcionDTO;
import com.duoc.cursosonline.dto.InscripcionResumenDTO;
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

    // INSCRIPCIONES

    public InscripcionDTO findInscripcionById(Long id) {

        return restTemplate.getForObject(
                consumerUrl + "/api/inscripciones/" + id,
                InscripcionDTO.class
        );
    }

    public List<InscripcionResumenDTO> findAllInscripciones() {

        ResponseEntity<List<InscripcionResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/inscripciones",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<InscripcionResumenDTO>>() {
                        });

        return response.getBody();
    }

    public List<InscripcionResumenDTO> findByCurso(Long id) {

        ResponseEntity<List<InscripcionResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/inscripciones/curso/" + id,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<InscripcionResumenDTO>>() {
                        });

        return response.getBody();
    }

    public List<InscripcionResumenDTO> findByFecha(LocalDate fecha) {

        ResponseEntity<List<InscripcionResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/inscripciones/fecha/" + fecha,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<InscripcionResumenDTO>>() {
                        });

        return response.getBody();
    }

    public byte[] descargarCertificado(Long id) {
        return restTemplate.exchange(
                consumerUrl + "/api/inscripciones/certificado/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<byte[]>() {
                }
        ).getBody();
    }

    // CURSOS

    public List<CursoResumenDTO> findAllCursos() {

        ResponseEntity<List<CursoResumenDTO>> response =
                restTemplate.exchange(
                        consumerUrl + "/api/cursos",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<CursoResumenDTO>>() {
                        });

        return response.getBody();
    }

    public CursoResumenDTO findCurso(Long id) {

        return restTemplate.getForObject(
                consumerUrl + "/api/cursos/" + id,
                CursoResumenDTO.class
        );
    }

}
