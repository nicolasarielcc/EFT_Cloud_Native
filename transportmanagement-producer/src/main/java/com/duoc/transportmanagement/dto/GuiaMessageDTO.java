package com.duoc.transportmanagement.dto;


public class GuiaMessageDTO {
    private String operacion;

    private Long id;

    private GuiaCreateDTO guiaCreate;

    private GuiaUpdateDTO guiaUpdate;

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

    public GuiaCreateDTO getGuiaCreate() {
        return guiaCreate;
    }

    public void setGuiaCreate(GuiaCreateDTO guiaCreate) {
        this.guiaCreate = guiaCreate;
    }

    public GuiaUpdateDTO getGuiaUpdate() {
        return guiaUpdate;
    }

    public void setGuiaUpdate(GuiaUpdateDTO guiaUpdate) {
        this.guiaUpdate = guiaUpdate;
    }
}
