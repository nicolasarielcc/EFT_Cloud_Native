package com.duoc.cursosonline.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer numeroInscripcion;

    private LocalDate fechaInscripcion;

    private String estudiante;

    private String correoEstudiante;

    private String estado;

    private Double calificacion;

    private String rutaEfs;

    private String rutaS3;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    public Inscripcion() {}

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

    public String getCorreoEstudiante() {
        return correoEstudiante;
    }

    public void setCorreoEstudiante(String correoEstudiante) {
        this.correoEstudiante = correoEstudiante;
    }

    public String getEstado() {
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

    public String getRutaEfs() {
        return rutaEfs;
    }

    public void setRutaEfs(String rutaEfs) {
        this.rutaEfs = rutaEfs;
    }

    public String getRutaS3() {
        return rutaS3;
    }

    public void setRutaS3(String rutaS3) {
        this.rutaS3 = rutaS3;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }


}
