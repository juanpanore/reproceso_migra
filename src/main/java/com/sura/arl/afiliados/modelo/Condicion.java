package com.sura.arl.afiliados.modelo;

public class Condicion {

    private String indicadorDias;
    private Integer ibcMaximo;
    private Integer ibcMinimo;
    private String tipoGeneracion;
    private String tipoTasa;
    private String tipoNovedad;
    private String periodoCotizacion;
    private String tipoCotizante;
    private String tipoAfiliado;

    public String getIndicadorDias() {
        return indicadorDias;
    }

    public void setIndicadorDias(String indicadorDias) {
        this.indicadorDias = indicadorDias;
    }

    public Integer getIbcMaximo() {
        return ibcMaximo;
    }

    public void setIbcMaximo(Integer ibcMaximo) {
        this.ibcMaximo = ibcMaximo;
    }

    public Integer getIbcMinimo() {
        return ibcMinimo;
    }

    public void setIbcMinimo(Integer ibcMinimo) {
        this.ibcMinimo = ibcMinimo;
    }

    public String getTipoGeneracion() {
        return tipoGeneracion;
    }

    public void setTipoGeneracion(String tipoGeneracion) {
        this.tipoGeneracion = tipoGeneracion;
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

    public String getPeriodoCotizacion() {
        return periodoCotizacion;
    }

    public void setPeriodoCotizacion(String periodoCotizacion) {
        this.periodoCotizacion = periodoCotizacion;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public String getTipoAfiliado() {
        return tipoAfiliado;
    }

    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
    }
    

    public Condicion cloneCondicion(){
        
        Condicion cndcn = new Condicion();
        cndcn.setIbcMaximo(this.getIbcMaximo());
        cndcn.setIbcMinimo(this.getIbcMinimo());
        cndcn.setIndicadorDias(this.getIndicadorDias());
        cndcn.setPeriodoCotizacion(this.getPeriodoCotizacion());
        cndcn.setTipoAfiliado(this.getTipoAfiliado());
        cndcn.setTipoCotizante(this.getTipoCotizante());
        cndcn.setTipoGeneracion(this.getTipoGeneracion());
        cndcn.setTipoNovedad(this.getTipoNovedad());
        cndcn.setTipoTasa(this.getTipoTasa());
                
        return cndcn;
    }
}
