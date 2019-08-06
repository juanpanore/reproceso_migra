package com.sura.arl.reproceso.modelo;

import java.util.Date;

public class InfoNovedadVCT {

    private String centroTrabajo;
    private Double totalNovedadesVct;
    private Date fechaInicioVCT;
    private Date fechaFinVCT;
    private String snvct;
    private boolean tieneVCT;

    public Double getTotalNovedadesVct() {
        return totalNovedadesVct;
    }

    public void setTotalNovedadesVct(Double totalNovedadesVct) {
        this.totalNovedadesVct = totalNovedadesVct;
    }

    public String getCentroTrabajo() {
        return centroTrabajo;
    }

    public void setCentroTrabajo(String centroTrabajo) {
        this.centroTrabajo = centroTrabajo;
    }

    public Date getFechaInicioVCT() {
        return fechaInicioVCT;
    }

    public void setFechaInicioVCT(Date fechaInicioVCT) {
        this.fechaInicioVCT = fechaInicioVCT;
    }

    public Date getFechaFinVCT() {
        return fechaFinVCT;
    }

    public void setFechaFinVCT(Date fechaFinVCT) {
        this.fechaFinVCT = fechaFinVCT;
    }

    public String getSnvct() {
        return snvct;
    }

    public void setSnvct(String snvct) {
        this.snvct = snvct;
    }
    
    public boolean tieneVCT() {
        return "X".equals(this.getSnvct());
    }
}
