package com.sura.arl.reproceso.modelo;

public class HistoricoCambioCT {
    private String poliza;
    private String dni;
    private String sucursalAnterior;
    private String sucursalNueva;
    private String periodosCambio;
    private String fuente;
    private String dniIngresa;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDniIngresa() {
        return dniIngresa;
    }

    public void setDniIngresa(String dniIngresa) {
        this.dniIngresa = dniIngresa;
    }

    public String getSucursalAnterior() {
        return sucursalAnterior;
    }

    public void setSucursalAnterior(String sucursalAnterior) {
        this.sucursalAnterior = sucursalAnterior;
    }

    public String getSucursalNueva() {
        return sucursalNueva;
    }

    public void setSucursalNueva(String sucursalNueva) {
        this.sucursalNueva = sucursalNueva;
    }

    public String getPeriodosCambio() {
        return periodosCambio;
    }

    public void setPeriodosCambio(String periodosCambio) {
        this.periodosCambio = periodosCambio;
    }

    public HistoricoCambioCT() {
        super();
    }

}
