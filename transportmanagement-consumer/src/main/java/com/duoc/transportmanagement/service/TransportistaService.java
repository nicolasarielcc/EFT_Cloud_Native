package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.TransportistaDTO;
import com.duoc.transportmanagement.dto.TransportistaResumenDTO;
import com.duoc.transportmanagement.exception.ResourceNotFoundException;
import com.duoc.transportmanagement.model.Transportista;
import com.duoc.transportmanagement.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportistaService {

    @Autowired
    private TransportistaRepository transportistaRepository;

    public List<TransportistaResumenDTO> findAll() {

        return transportistaRepository.findAll()
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public TransportistaResumenDTO findById(Long id) {

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transportista no encontrado"));

        return toResumenDTO(transportista);
    }

    public TransportistaResumenDTO saveTransportista(TransportistaDTO dto) {

        Transportista transportista = new Transportista();
        transportista.setNombre(dto.getNombre());
        transportista.setRut(dto.getRut());
        transportista.setTelefono(dto.getTelefono());
        transportista.setCorreo(dto.getCorreo());

        transportistaRepository.save(transportista);

        return toResumenDTO(transportista);
    }

    public TransportistaResumenDTO updateTransportista(Long id,
                                                       TransportistaDTO dto) {

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transportista no encontrado"));

        transportista.setNombre(dto.getNombre());
        transportista.setRut(dto.getRut());
        transportista.setTelefono(dto.getTelefono());
        transportista.setCorreo(dto.getCorreo());

        transportistaRepository.save(transportista);

        return toResumenDTO(transportista);
    }

    public void deleteTransportista(Long id) {

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transportista no encontrado"));

        transportistaRepository.delete(transportista);
    }

    private TransportistaResumenDTO toResumenDTO(Transportista transportista) {

        TransportistaResumenDTO dto =
                new TransportistaResumenDTO();

        dto.setId(transportista.getId());
        dto.setNombre(transportista.getNombre());

        return dto;
    }
}
