package com.sura.arl.integrador.modelo;

public class Registro {

    private long id;
    private String dni;
    private String poliza;
    private String dsParametros;
    private String mensajeMQ;
    private String estado;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getMensajeMQ() {
        return mensajeMQ;
    }

    public void setMensajeMQ(String mensajeMQ) {
        this.mensajeMQ = mensajeMQ;
    }

    public String getDsParametros() {
        return dsParametros;
    }

    public void setDsParametros(String dsParametros) {
        this.dsParametros = dsParametros;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
