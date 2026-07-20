package com.duoc.transportmanagement.service;

import com.duoc.transportmanagement.dto.*;
import com.duoc.transportmanagement.exception.ResourceNotFoundException;
import com.duoc.transportmanagement.model.GuiaDespacho;
import com.duoc.transportmanagement.model.Transportista;
import com.duoc.transportmanagement.repository.GuiaRepository;
import com.duoc.transportmanagement.repository.S3Repository;
import com.duoc.transportmanagement.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class GuiaService {

    @Autowired
    private GuiaRepository guiaRepository;

    @Autowired
    private TransportistaRepository transportistaRepository;

    @Autowired
    private S3Repository s3Repository;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${efs.path:/app/efs}")
    private String efsPath;

    public List<GuiaResumenDTO> findAll() {

        return guiaRepository.findAll()
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public GuiaDTO findById(Long id) {

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        return toDTO(guia);
    }

    public List<GuiaResumenDTO> findByFecha(LocalDate fecha) {

        return guiaRepository.findByFechaGeneracion(fecha)
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public GuiaDTO createGuia(GuiaCreateDTO dto) {

        Transportista transportista =
                transportistaRepository.findById(dto.getTransportistaId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Transportista no encontrado"));

        GuiaDespacho guia = new GuiaDespacho();

        guia.setNumeroGuia(dto.getNumeroGuia());
        guia.setCliente(dto.getCliente());
        guia.setDireccionEntrega(dto.getDireccionEntrega());
        guia.setDescripcionCarga(dto.getDescripcionCarga());
        guia.setFechaGeneracion(LocalDate.now());
        guia.setEstado("GENERADA");
        guia.setTransportista(transportista);

        String nombreTransportista = transportista.getNombre()
                .toLowerCase()
                .replace(" ", "-");

        String rutaEfs =
                "/app/efs/guias/guia_" +
                        dto.getNumeroGuia() +
                        ".txt";

        String rutaS3 =
                "guias/" +
                        LocalDate.now().getYear() + "/" +
                        String.format("%02d", LocalDate.now().getMonthValue()) + "/" +
                        String.format("%02d", LocalDate.now().getDayOfMonth()) + "/" +
                        nombreTransportista +
                        "/guia_" +
                        dto.getNumeroGuia() +
                        ".txt";

        guia.setRutaEfs(rutaEfs);
        guia.setRutaS3(rutaS3);

        guiaRepository.save(guia);

        return toDTO(guia);
    }

    public GuiaDTO updateGuia(Long id, GuiaUpdateDTO dto) {

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        guia.setCliente(dto.getCliente());
        guia.setDireccionEntrega(dto.getDireccionEntrega());
        guia.setDescripcionCarga(dto.getDescripcionCarga());

        if(dto.getEstado() != null){
            guia.setEstado(dto.getEstado() ? "ACTIVA" : "INACTIVA");
        }

        guiaRepository.save(guia);

        return toDTO(guia);
    }

    public void deleteGuia(Long id) {

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        if(guia.getRutaS3() != null){

            s3Repository.deleteObject(
                    bucketName,
                    guia.getRutaS3()
            );
        }

        guiaRepository.delete(guia);
    }

    public List<GuiaResumenDTO> findByTransportista(Long transportistaId){

        return guiaRepository.findByTransportistaId(transportistaId)
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public ArchivoDTO generarArchivo(Long id){

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        try {

            String carpeta =
                    efsPath +
                            "/" +
                            LocalDate.now().getYear();

            File directorio = new File(carpeta);

            if(!directorio.exists()){
                directorio.mkdirs();
            }

            String nombreArchivo =
                    "guia_" +
                            guia.getNumeroGuia() +
                            ".txt";

            File archivo = new File(
                    carpeta + "/" + nombreArchivo
            );

            try(FileWriter writer = new FileWriter(archivo)){

                writer.write(
                        generarContenidoGuia(id)
                );
            }

            guia.setRutaEfs(
                    archivo.getAbsolutePath()
            );

            guiaRepository.save(guia);

            ArchivoDTO dto = new ArchivoDTO();

            dto.setNombreArchivo(nombreArchivo);
            dto.setRutaEfs(archivo.getAbsolutePath());

            return dto;

        } catch (IOException e){

            throw new RuntimeException(
                    "Error al generar archivo",
                    e
            );
        }
    }

    public String subirArchivoS3(Long id){

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        generarArchivo(id);

        guia = guiaRepository.findById(id).get();


        File archivo = new File(
                guia.getRutaEfs()
        );

        LocalDate fecha = guia.getFechaGeneracion();

        String key =
                fecha.getYear()
                        + "/"
                        + String.format("%02d", fecha.getMonthValue())
                        + "/"
                        + String.format("%02d", LocalDate.now().getDayOfMonth())
                        + "/"
                        + guia.getTransportista().getNombre()
                        + "/"
                        + archivo.getName();

        String resultado =
                s3Repository.uploadFile(
                        bucketName,
                        key,
                        archivo
                );

        guia.setRutaS3(key);

        guiaRepository.save(guia);

        return resultado;
    }

    public String actualizarArchivoS3(Long id, GuiaUpdateDTO dto){


        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        if(guia.getRutaS3() != null){

            s3Repository.deleteObject(
                    bucketName,
                    guia.getRutaS3()
            );
        }
        updateGuia(id, dto);
        generarArchivo(id);

        return subirArchivoS3(id);
    }

    public byte[] descargarArchivo(Long id){

        try {

            GuiaDespacho guia = guiaRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Guía no encontrada"));

            return s3Repository.downloadFile(
                    bucketName,
                    guia.getRutaS3()
            );

        } catch (IOException e){

            throw new RuntimeException(
                    "Error al descargar archivo",
                    e
            );
        }
    }

    public void eliminarArchivoS3(Long id){

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        if(guia.getRutaS3() != null){

            s3Repository.deleteObject(
                    bucketName,
                    guia.getRutaS3()
            );

            guia.setRutaS3(null);

            guiaRepository.save(guia);
        }
    }

    private String generarContenidoGuia(Long id){

        GuiaDespacho guia = guiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guía no encontrada"));

        StringBuilder contenido = new StringBuilder();

        contenido.append("GUIA DE DESPACHO\n\n");
        contenido.append("Numero Guia: ")
                .append(guia.getNumeroGuia())
                .append("\n");

        contenido.append("Fecha: ")
                .append(guia.getFechaGeneracion())
                .append("\n");

        contenido.append("Cliente: ")
                .append(guia.getCliente())
                .append("\n");

        contenido.append("Direccion: ")
                .append(guia.getDireccionEntrega())
                .append("\n");

        contenido.append("Carga: ")
                .append(guia.getDescripcionCarga())
                .append("\n");

        contenido.append("Transportista: ")
                .append(guia.getTransportista().getNombre())
                .append("\n");

        contenido.append("Estado: ")
                .append(guia.getEstado());

        return contenido.toString();
    }

    private GuiaDTO toDTO(GuiaDespacho guia){

        GuiaDTO dto = new GuiaDTO();

        dto.setId(guia.getId());
        dto.setNumeroGuia(guia.getNumeroGuia());
        dto.setFechaGeneracion(guia.getFechaGeneracion());
        dto.setEstado(guia.getEstado());

        dto.setCliente(guia.getCliente());
        dto.setDireccionEntrega(guia.getDireccionEntrega());
        dto.setDescripcionCarga(guia.getDescripcionCarga());

        dto.setRutaEfs(guia.getRutaEfs());
        dto.setRutaS3(guia.getRutaS3());

        dto.setTransportistaId(
                guia.getTransportista().getId()
        );

        dto.setTransportistaNombre(
                guia.getTransportista().getNombre()
        );

        return dto;
    }

    private GuiaResumenDTO toResumenDTO(
            GuiaDespacho guia){

        GuiaResumenDTO dto =
                new GuiaResumenDTO();

        dto.setId(guia.getId());

        dto.setNumeroGuia(
                guia.getNumeroGuia()
        );

        dto.setCliente(
                guia.getCliente()
        );

        dto.setTransportista(
                guia.getTransportista()
                        .getNombre()
        );

        dto.setEstado(guia.getEstado());

        dto.setFechaGeneracion(guia.getFechaGeneracion());

        return dto;
    }
}
