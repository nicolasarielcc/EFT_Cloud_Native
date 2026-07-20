package com.duoc.transportmanagement.dto;

public class TransportistaMessageDTO {
    private String operacion;

    private Long id;

    private TransportistaDTO transportistaDTO;

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

    public TransportistaDTO getTransportistaDTO() {
        return transportistaDTO;
    }

    public void setTransportistaDTO(TransportistaDTO transportistaDTO) {
        this.transportistaDTO = transportistaDTO;
    }
}
