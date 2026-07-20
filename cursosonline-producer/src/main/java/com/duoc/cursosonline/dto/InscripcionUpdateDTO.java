package com.duoc.cursosonline.dto;

public class InscripcionUpdateDTO {
    private String estudiante;

    private String correoEstudiante;

    private Double calificacion;

    private Boolean estado;

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

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
