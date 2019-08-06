package com.sura.arl.estadocuenta.modelo;

public class CondicionesTipoCotizante {

    private Integer cantidadMinimaSalarios;
    private Integer cantidadMaximaSalarios;
    private String tipoGeneracion;
    private String tipoCotizante;
    private String indicadorProporcionalDias;
    private String tipoTasa;
    private String tipoNovedad;

    public Integer getCantidadMinimaSalarios() {
        return cantidadMinimaSalarios;
    }

    public void setCantidadMinimaSalarios(Integer cantidadMinimaSalarios) {
        this.cantidadMinimaSalarios = cantidadMinimaSalarios;
    }

    public Integer getCantidadMaximaSalarios() {
        return cantidadMaximaSalarios;
    }

    public void setCantidadMaximaSalarios(Integer cantidadMaximaSalarios) {
        this.cantidadMaximaSalarios = cantidadMaximaSalarios;
    }

    public String getTipoGeneracion() {
        return tipoGeneracion;
    }

    public void setTipoGeneracion(String tipoGeneracion) {
        this.tipoGeneracion = tipoGeneracion;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public String getIndicadorProporcionalDias() {
        return indicadorProporcionalDias;
    }

    public void setIndicadorProporcionalDias(String indicadorProporcionalDias) {
        this.indicadorProporcionalDias = indicadorProporcionalDias;
    }

    public String getTipoTasa() {
        return tipoTasa;
    }

    public void setTipoTasa(String tipoTasa) {
        this.tipoTasa = tipoTasa;
    }

    public String getTipoNovedad() {
        return tipoNovedad;
    }

    public void setTipoNovedad(String tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }

}
