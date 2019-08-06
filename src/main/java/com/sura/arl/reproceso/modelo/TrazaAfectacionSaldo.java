package com.sura.arl.reproceso.modelo;

public class TrazaAfectacionSaldo {
    private String poliza;
    private String periodo;
    private EstadoTrazaAfectacionSaldo estado;
    private Double valor;
    private String observacion;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public EstadoTrazaAfectacionSaldo getEstado() {
        return estado;
    }

    public void setEstado(EstadoTrazaAfectacionSaldo estado) {
        this.estado = estado;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

}
