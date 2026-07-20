package com.duoc.transportmanagement.dto;

public class ArchivoDTO {
    private String nombreArchivo;

    private String rutaEfs;

    private String rutaS3;

    private String url;

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
