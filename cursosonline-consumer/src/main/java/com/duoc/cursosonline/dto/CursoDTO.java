package com.duoc.cursosonline.dto;

public class CursoDTO {
    private String nombre;

    private String instructor;

    private String categoria;

    private String correoInstructor;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCorreoInstructor() {
        return correoInstructor;
    }

    public void setCorreoInstructor(String correoInstructor) {
        this.correoInstructor = correoInstructor;
    }
}
