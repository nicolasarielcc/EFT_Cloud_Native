package com.duoc.cursosonline.dto;

public class InscripcionCreateDTO {
    private Integer numeroInscripcion;

    private String estudiante;

    private String correoEstudiante;

    private Long cursoId;

    //GETTERS Y SETTERS
    public Integer getNumeroInscripcion() {
        return numeroInscripcion;
    }

    public void setNumeroInscripcion(Integer numeroInscripcion) {
        this.numeroInscripcion = numeroInscripcion;
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

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }
}
