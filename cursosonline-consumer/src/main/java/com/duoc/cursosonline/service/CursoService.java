package com.duoc.cursosonline.service;

import com.duoc.cursosonline.dto.CursoDTO;
import com.duoc.cursosonline.dto.CursoResumenDTO;
import com.duoc.cursosonline.exception.ResourceNotFoundException;
import com.duoc.cursosonline.model.Curso;
import com.duoc.cursosonline.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    public List<CursoResumenDTO> findAll() {

        return cursoRepository.findAll()
                .stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public CursoResumenDTO findById(Long id) {

        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Curso no encontrado"));

        return toResumenDTO(curso);
    }

    public CursoResumenDTO saveCurso(CursoDTO dto) {

        Curso curso = new Curso();
        curso.setNombre(dto.getNombre());
        curso.setInstructor(dto.getInstructor());
        curso.setCategoria(dto.getCategoria());
        curso.setCorreoInstructor(dto.getCorreoInstructor());

        cursoRepository.save(curso);

        return toResumenDTO(curso);
    }

    public CursoResumenDTO updateCurso(Long id,
                                       CursoDTO dto) {

        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Curso no encontrado"));

        curso.setNombre(dto.getNombre());
        curso.setInstructor(dto.getInstructor());
        curso.setCategoria(dto.getCategoria());
        curso.setCorreoInstructor(dto.getCorreoInstructor());

        cursoRepository.save(curso);

        return toResumenDTO(curso);
    }

    public void deleteCurso(Long id) {

        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Curso no encontrado"));

        cursoRepository.delete(curso);
    }

    private CursoResumenDTO toResumenDTO(Curso curso) {

        CursoResumenDTO dto =
                new CursoResumenDTO();

        dto.setId(curso.getId());
        dto.setNombre(curso.getNombre());

        return dto;
    }
}
