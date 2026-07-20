package com.duoc.cursosonline.dto;

public class CursoMessageDTO {
    private String operacion;

    private Long id;

    private CursoDTO cursoDTO;

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

    public CursoDTO getCursoDTO() {
        return cursoDTO;
    }

    public void setCursoDTO(CursoDTO cursoDTO) {
        this.cursoDTO = cursoDTO;
    }
}
