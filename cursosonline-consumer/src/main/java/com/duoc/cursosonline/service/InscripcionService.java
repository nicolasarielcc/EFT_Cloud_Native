package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.*;
import com.duoc.cursosonline.exception.ResourceNotFoundException;
import com.duoc.cursosonline.model.Curso;
import com.duoc.cursosonline.model.Inscripcion;
import com.duoc.cursosonline.repository.CursoRepository;
import com.duoc.cursosonline.repository.InscripcionRepository;
import com.duoc.cursosonline.repository.S3Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private S3Repository s3Repository;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${efs.path:/app/efs}")
    private String efsPath;

    public List<InscripcionResumenDTO> findAll() {

        return inscripcionRepository.findAll()
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public InscripcionDTO findById(Long id) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        return toDTO(inscripcion);
    }

    public List<InscripcionResumenDTO> findByFecha(LocalDate fecha) {

        return inscripcionRepository.findByFechaInscripcion(fecha)
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public InscripcionDTO createInscripcion(InscripcionCreateDTO dto) {

        Curso curso =
                cursoRepository.findById(dto.getCursoId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Curso no encontrado"));

        Inscripcion inscripcion = new Inscripcion();

        inscripcion.setNumeroInscripcion(dto.getNumeroInscripcion());
        inscripcion.setEstudiante(dto.getEstudiante());
        inscripcion.setCorreoEstudiante(dto.getCorreoEstudiante());
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setEstado("INSCRITA");
        inscripcion.setCurso(curso);

        String nombreCurso = curso.getNombre()
                .toLowerCase()
                .replace(" ", "-");

        String rutaEfs =
                "/app/efs/certificados/certificado_" +
                        dto.getNumeroInscripcion() +
                        ".txt";

        String rutaS3 =
                "certificados/" +
                        LocalDate.now().getYear() + "/" +
                        String.format("%02d", LocalDate.now().getMonthValue()) + "/" +
                        String.format("%02d", LocalDate.now().getDayOfMonth()) + "/" +
                        nombreCurso +
                        "/certificado_" +
                        dto.getNumeroInscripcion() +
                        ".txt";

        inscripcion.setRutaEfs(rutaEfs);
        inscripcion.setRutaS3(rutaS3);

        inscripcionRepository.save(inscripcion);

        return toDTO(inscripcion);
    }

    public InscripcionDTO updateInscripcion(Long id, InscripcionUpdateDTO dto) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        inscripcion.setEstudiante(dto.getEstudiante());
        inscripcion.setCorreoEstudiante(dto.getCorreoEstudiante());

        if(dto.getCalificacion() != null){
            inscripcion.setCalificacion(dto.getCalificacion());
        }

        if(dto.getEstado() != null){
            inscripcion.setEstado(dto.getEstado() ? "COMPLETADA" : "ANULADA");
        }

        inscripcionRepository.save(inscripcion);

        return toDTO(inscripcion);
    }

    public void deleteInscripcion(Long id) {

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        if(inscripcion.getRutaS3() != null){

            s3Repository.deleteObject(
                    bucketName,
                    inscripcion.getRutaS3()
            );
        }

        inscripcionRepository.delete(inscripcion);
    }

    public List<InscripcionResumenDTO> findByCurso(Long cursoId){

        return inscripcionRepository.findByCursoId(cursoId)
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public ArchivoDTO generarCertificado(Long id){

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

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
                    "certificado_" +
                            inscripcion.getNumeroInscripcion() +
                            ".txt";

            File archivo = new File(
                    carpeta + "/" + nombreArchivo
            );

            try(FileWriter writer = new FileWriter(archivo)){

                writer.write(
                        generarContenidoCertificado(id)
                );
            }

            inscripcion.setRutaEfs(
                    archivo.getAbsolutePath()
            );

            inscripcionRepository.save(inscripcion);

            ArchivoDTO dto = new ArchivoDTO();

            dto.setNombreArchivo(nombreArchivo);
            dto.setRutaEfs(archivo.getAbsolutePath());

            return dto;

        } catch (IOException e){

            throw new RuntimeException(
                    "Error al generar certificado",
                    e
            );
        }
    }

    public String subirCertificadoS3(Long id){

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        generarCertificado(id);

        inscripcion = inscripcionRepository.findById(id).get();


        File archivo = new File(
                inscripcion.getRutaEfs()
        );

        LocalDate fecha = inscripcion.getFechaInscripcion();

        String key =
                fecha.getYear()
                        + "/"
                        + String.format("%02d", fecha.getMonthValue())
                        + "/"
                        + String.format("%02d", LocalDate.now().getDayOfMonth())
                        + "/"
                        + inscripcion.getCurso().getNombre()
                        + "/"
                        + archivo.getName();

        String resultado =
                s3Repository.uploadFile(
                        bucketName,
                        key,
                        archivo
                );

        inscripcion.setRutaS3(key);

        inscripcionRepository.save(inscripcion);

        return resultado;
    }

    public String actualizarCertificadoS3(Long id, InscripcionUpdateDTO dto){


        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        if(inscripcion.getRutaS3() != null){

            s3Repository.deleteObject(
                    bucketName,
                    inscripcion.getRutaS3()
            );
        }
        updateInscripcion(id, dto);
        generarCertificado(id);

        return subirCertificadoS3(id);
    }

    public byte[] descargarCertificado(Long id){

        try {

            Inscripcion inscripcion = inscripcionRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Inscripción no encontrada"));

            return s3Repository.downloadFile(
                    bucketName,
                    inscripcion.getRutaS3()
            );

        } catch (IOException e){

            throw new RuntimeException(
                    "Error al descargar certificado",
                    e
            );
        }
    }

    public void eliminarCertificadoS3(Long id){

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        if(inscripcion.getRutaS3() != null){

            s3Repository.deleteObject(
                    bucketName,
                    inscripcion.getRutaS3()
            );

            inscripcion.setRutaS3(null);

            inscripcionRepository.save(inscripcion);
        }
    }

    private String generarContenidoCertificado(Long id){

        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inscripción no encontrada"));

        StringBuilder contenido = new StringBuilder();

        contenido.append("CERTIFICADO DE APROBACION\n\n");
        contenido.append("Numero Inscripcion: ")
                .append(inscripcion.getNumeroInscripcion())
                .append("\n");

        contenido.append("Fecha Inscripcion: ")
                .append(inscripcion.getFechaInscripcion())
                .append("\n");

        contenido.append("Estudiante: ")
                .append(inscripcion.getEstudiante())
                .append("\n");

        contenido.append("Correo: ")
                .append(inscripcion.getCorreoEstudiante())
                .append("\n");

        contenido.append("Curso: ")
                .append(inscripcion.getCurso().getNombre())
                .append("\n");

        contenido.append("Instructor: ")
                .append(inscripcion.getCurso().getInstructor())
                .append("\n");

        contenido.append("Calificacion Final: ")
                .append(inscripcion.getCalificacion())
                .append("\n");

        contenido.append("Estado: ")
                .append(inscripcion.getEstado());

        return contenido.toString();
    }

    private InscripcionDTO toDTO(Inscripcion inscripcion){

        InscripcionDTO dto = new InscripcionDTO();

        dto.setId(inscripcion.getId());
        dto.setNumeroInscripcion(inscripcion.getNumeroInscripcion());
        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());
        dto.setEstado(inscripcion.getEstado());

        dto.setEstudiante(inscripcion.getEstudiante());
        dto.setCorreoEstudiante(inscripcion.getCorreoEstudiante());
        dto.setCalificacion(inscripcion.getCalificacion());

        dto.setRutaEfs(inscripcion.getRutaEfs());
        dto.setRutaS3(inscripcion.getRutaS3());

        dto.setCursoId(
                inscripcion.getCurso().getId()
        );

        dto.setCursoNombre(
                inscripcion.getCurso().getNombre()
        );

        return dto;
    }

    private InscripcionResumenDTO toResumenDTO(
            Inscripcion inscripcion){

        InscripcionResumenDTO dto =
                new InscripcionResumenDTO();

        dto.setId(inscripcion.getId());

        dto.setNumeroInscripcion(
                inscripcion.getNumeroInscripcion()
        );

        dto.setEstudiante(
                inscripcion.getEstudiante()
        );

        dto.setCurso(
                inscripcion.getCurso()
                        .getNombre()
        );

        dto.setEstado(inscripcion.getEstado());

        dto.setCalificacion(inscripcion.getCalificacion());

        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());

        return dto;
    }
}
