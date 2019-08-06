package com.sura.arl.reproceso.modelo;

import java.util.Date;

public class Consolidado {
    private String poliza;
    private Double saldoAfavor;
    private Double deuda;
    private Double otrosConceptos;
    private Double totalRenes;
    private Double valorAnulado;
    private Double valorEsperado;
    private Double valorEsperadoInicial;
    private String periodo;
    private Date fechaLimitePago;
    private String tipoAfiliado;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public Double getSaldoAfavor() {
        return saldoAfavor;
    }

    public void setSaldoAfavor(Double saldoAfavor) {
        this.saldoAfavor = saldoAfavor;
    }

    public Double getDeuda() {
        return deuda;
    }

    public void setDeuda(Double deuda) {
        this.deuda = deuda;
    }

    public Double getOtrosConceptos() {
        return otrosConceptos;
    }

    public void setOtrosConceptos(Double otrosConceptos) {
        this.otrosConceptos = otrosConceptos;
    }

    public Double getTotalRenes() {
        return totalRenes;
    }

    public void setTotalRenes(Double totalRenes) {
        this.totalRenes = totalRenes;
    }

    public Double getValorAnulado() {
        return valorAnulado;
    }

    public void setValorAnulado(Double valorAnulado) {
        this.valorAnulado = valorAnulado;
    }

    public Double getValorEsperado() {
        return valorEsperado;
    }

    public void setValorEsperado(Double valorEsperado) {
        this.valorEsperado = valorEsperado;
    }

    public Double getValorEsperadoInicial() {
        return valorEsperadoInicial;
    }

    public void setValorEsperadoInicial(Double valorEsperadoInicial) {
        this.valorEsperadoInicial = valorEsperadoInicial;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Date getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(Date fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    public String getTipoAfiliado() {
        return tipoAfiliado;
    }

    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
    }
    
    
}
