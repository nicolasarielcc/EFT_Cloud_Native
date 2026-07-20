package com.duoc.cursosonline.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El instructor es obligatorio")
    private String instructor;

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    @NotBlank(message = "El correo del instructor es obligatorio")
    private String correoInstructor;

    public Curso() {}

    public Curso(Long id, String nombre, String instructor, String categoria, String correoInstructor) {
        this.id = id;
        this.nombre = nombre;
        this.instructor = instructor;
        this.categoria = categoria;
        this.correoInstructor = correoInstructor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
