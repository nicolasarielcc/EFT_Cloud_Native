package com.duoc.transportmanagement.dto;

import java.time.LocalDate;
import java.util.Date;

public class GuiaDTO {
    private Long id;

    private Integer numeroGuia;

    private LocalDate fechaGeneracion;

    private String cliente;

    private String direccionEntrega;

    private String descripcionCarga;

    private String estado;

    private String rutaEfs;

    private String rutaS3;

    private Long transportistaId;

    private String transportistaNombre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(Integer numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
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

    public String isEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public Long getTransportistaId() {
        return transportistaId;
    }

    public void setTransportistaId(Long transportistaId) {
        this.transportistaId = transportistaId;
    }

    public String getTransportistaNombre() {
        return transportistaNombre;
    }

    public void setTransportistaNombre(String transportistaNombre) {
        this.transportistaNombre = transportistaNombre;
    }
}
