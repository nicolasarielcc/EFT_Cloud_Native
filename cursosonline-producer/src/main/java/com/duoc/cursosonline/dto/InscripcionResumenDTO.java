package com.duoc.cursosonline.dto;

import java.time.LocalDate;

public class InscripcionResumenDTO {
    private Long id;

    private Integer numeroInscripcion;

    private LocalDate fechaInscripcion;

    private String estudiante;

    private String curso;

    private String estado;

    private Double calificacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroInscripcion() {
        return numeroInscripcion;
    }

    public void setNumeroInscripcion(Integer numeroInscripcion) {
        this.numeroInscripcion = numeroInscripcion;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(String estudiante) {
        this.estudiante = estudiante;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String isEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }
}
