package com.sura.arl.afiliados.modelo;

import java.util.Date;

public class Cobertura {

    private String poliza;
    private String periodo;
    private Date fechaInicioProceso;
    private Date fechaLimitePago;
    private String sucursal;
    private String sucursalPagadora;
    private String periodoGeneracion;
    private String tipoPoliza;
    private String tieneAfiliacion;
    private Long salario;
    private Long ultimoIbcCotizado;

    private Date fealta;
    private Date febaja;
    private String esMismoPeriodoDeAlta;
    private String periodoEsMenorFealta;
    
    private int totalCoberturas;

    public Cobertura() {
        super();
    }

    public Cobertura(String poliza) {
        this(poliza, null);
    }

    public Cobertura(String poliza, String periodo) {
        this.poliza = poliza;
        this.periodo = periodo;
    }

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getPeriodo() {
        return periodo;
    }

    public String getPeriodoAnioMes() {
        if (periodo != null) {
            return periodo.substring(2, 6).concat(periodo.substring(0, 2));
        }
        return null;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Date getFechaInicioProceso() {
        return fechaInicioProceso;
    }

    public void setFechaInicioProceso(Date fechaInicioProceso) {
        this.fechaInicioProceso = fechaInicioProceso;
    }

    public Date getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(Date fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getSucursalPagadora() {
        return sucursalPagadora;
    }

    public void setSucursalPagadora(String sucursalPagadora) {
        this.sucursalPagadora = sucursalPagadora;
    }

    public String getPeriodoGeneracion() {
        return periodoGeneracion;
    }

    public void setPeriodoGeneracion(String periodoGeneracion) {
        this.periodoGeneracion = periodoGeneracion;
    }

    public String getTipoPoliza() {
        return tipoPoliza;
    }

    public void setTipoPoliza(String tipoPoliza) {
        this.tipoPoliza = tipoPoliza;
    }

    public String getTieneAfiliacion() {
        return tieneAfiliacion;
    }

    public void setTieneAfiliacion(String tieneAfiliacion) {
        this.tieneAfiliacion = tieneAfiliacion;
    }

    public Long getSalario() {
        return salario;
    }

    public void setSalario(Long salario) {
        this.salario = salario;
    }

    public Long getUltimoIbcCotizado() {
        return ultimoIbcCotizado;
    }

    public void setUltimoIbcCotizado(Long ultimoIbcCotizado) {
        this.ultimoIbcCotizado = ultimoIbcCotizado;
    }

    public Date getFealta() {
        return fealta;
    }

    public void setFealta(Date fealta) {
        this.fealta = fealta;
    }

    public Date getFebaja() {
        return febaja;
    }

    public void setFebaja(Date febaja) {
        this.febaja = febaja;
    }

    public String esMismoPeriodoDeAlta() {
        return esMismoPeriodoDeAlta;
    }

    public void setEsMismoPeriodoDeAlta(String esMismoPeriodoDeAlta) {
        this.esMismoPeriodoDeAlta = esMismoPeriodoDeAlta;
    }

    public String periodoEsMenorFealta() {
        return periodoEsMenorFealta;
    }

    public void setPeriodoEsMenorFealta(String periodoEsMenorFealta) {
        this.periodoEsMenorFealta = periodoEsMenorFealta;
    }

    public int getTotalCoberturas() {
        return totalCoberturas;
    }

    public void setTotalCoberturas(int totalCoberturas) {
        this.totalCoberturas = totalCoberturas;
    }

    public Cobertura cloneCobertura() {
        Cobertura cbrtr = new Cobertura();

        cbrtr.setEsMismoPeriodoDeAlta(this.esMismoPeriodoDeAlta());
        cbrtr.setFealta(this.getFealta());
        cbrtr.setFebaja(this.getFebaja());
        cbrtr.setFechaInicioProceso(this.getFechaInicioProceso());
        cbrtr.setFechaLimitePago(this.getFechaLimitePago());
        cbrtr.setPeriodo(this.getPeriodo());
        cbrtr.setPeriodoEsMenorFealta(this.esMismoPeriodoDeAlta());
        cbrtr.setPeriodoGeneracion(this.getPeriodoGeneracion());
        cbrtr.setPoliza(this.getPoliza());
        cbrtr.setSalario(this.getSalario());
        cbrtr.setSucursal(this.getSucursal());
        cbrtr.setSucursalPagadora(this.getSucursalPagadora());
        cbrtr.setTieneAfiliacion(this.getTieneAfiliacion());
        cbrtr.setTipoPoliza(this.getTipoPoliza());
        cbrtr.setUltimoIbcCotizado(this.getUltimoIbcCotizado());
        cbrtr.setTotalCoberturas(this.getTotalCoberturas());


        return cbrtr;
    }

}
