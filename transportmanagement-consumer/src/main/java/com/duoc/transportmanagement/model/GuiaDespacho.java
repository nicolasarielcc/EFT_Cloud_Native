package com.duoc.transportmanagement.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class GuiaDespacho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer numeroGuia;

    private LocalDate fechaGeneracion;

    private String cliente;

    private String direccionEntrega;

    private String descripcionCarga;

    private String estado;

    private String rutaEfs;

    private String rutaS3;

    @ManyToOne
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;

    public GuiaDespacho() {}

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

    public String getEstado() {
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

    public Transportista getTransportista() {
        return transportista;
    }

    public void setTransportista(Transportista transportista) {
        this.transportista = transportista;
    }


}
