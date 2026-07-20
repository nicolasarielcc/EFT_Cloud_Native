package com.duoc.cursosonline.dto;


public class InscripcionMessageDTO {
    private String operacion;

    private Long id;

    private InscripcionCreateDTO inscripcionCreate;

    private InscripcionUpdateDTO inscripcionUpdate;

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InscripcionCreateDTO getInscripcionCreate() {
        return inscripcionCreate;
    }

    public void setInscripcionCreate(InscripcionCreateDTO inscripcionCreate) {
        this.inscripcionCreate = inscripcionCreate;
    }

    public InscripcionUpdateDTO getInscripcionUpdate() {
        return inscripcionUpdate;
    }

    public void setInscripcionUpdate(InscripcionUpdateDTO inscripcionUpdate) {
        this.inscripcionUpdate = inscripcionUpdate;
    }
}
