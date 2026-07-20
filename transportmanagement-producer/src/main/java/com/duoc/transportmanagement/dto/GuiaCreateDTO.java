package com.duoc.transportmanagement.dto;

public class GuiaCreateDTO {
    private Integer numeroGuia;

    private String cliente;

    private String direccionEntrega;

    private String descripcionCarga;

    private Long transportistaId;

    //GETTERS Y SETTERS
    public Integer getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(Integer numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getDescripcionCarga() {
        return descripcionCarga;
    }

    public void setDescripcionCarga(String descripcionCarga) {
        this.descripcionCarga = descripcionCarga;
    }

    public Long getTransportistaId() {
        return transportistaId;
    }

    public void setTransportistaId(Long transportistaId) {
        this.transportistaId = transportistaId;
    }
}
