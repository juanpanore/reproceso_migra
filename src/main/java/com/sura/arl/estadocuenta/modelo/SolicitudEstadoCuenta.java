package com.sura.arl.estadocuenta.modelo;

public class SolicitudEstadoCuenta {

    private String dniAfiliado;
    private String poliza;
    private String periodoCotizacion;
    private String periodoGeneracion;
    private String certificadoAfiliado;

    public String getDniAfiliado() {
        return dniAfiliado;
    }

    public void setDniAfiliado(String dniAfiliado) {
        this.dniAfiliado = dniAfiliado;
    }

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getPeriodoCotizacion() {
        return periodoCotizacion;
    }

    public void setPeriodoCotizacion(String periodoCotizacion) {
        this.periodoCotizacion = periodoCotizacion;
    }

    public String getPeriodoGeneracion() {
        return periodoGeneracion;
    }

    public void setPeriodoGeneracion(String periodoGeneracion) {
        this.periodoGeneracion = periodoGeneracion;
    }

    public String getCertificadoAfiliado() {
        return certificadoAfiliado;
    }

    public void setCertificadoAfiliado(String certificadoAfiliado) {
        this.certificadoAfiliado = certificadoAfiliado;
    }

}
