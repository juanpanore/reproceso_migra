package com.sura.arl.reproceso.modelo;

import java.util.Date;

public class DatosNovedades {
    private Double ibc;
    private Double dias;
    private Double salario;
    private Double tasa;
    private Long cotizacion;
    private boolean ingreso;
    private boolean retiro;
    private Date fechaPago;
    private TipoPlanilla tipoPlanilla;
    private String tipoCotizante;
    private String subTipoCotizante;
    private Long numeroFormulario;
    private String planilla;
    private String responsable;
    private String periodo;
    private Long consecutivo;
    private String id;
    private String ley;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getIbc() {
        return ibc;
    }

    public void setIbc(Double ibc) {
        this.ibc = ibc;
    }

    public Double getDias() {
        return dias;
    }

    public void setDias(Double dias) {
        this.dias = dias;
    }

    public Double getSalario() {
        return salario;
    }

    public void setSalario(Double salario) {
        this.salario = salario;
    }

    public Double getTasa() {
        return tasa;
    }

    public void setTasa(Double tasa) {
        this.tasa = tasa;
    }

    public Long getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Long cotizacion) {
        this.cotizacion = cotizacion;
    }

    public boolean isIngreso() {
        return ingreso;
    }

    public void setIngreso(boolean ingreso) {
        this.ingreso = ingreso;
    }

    public boolean isRetiro() {
        return retiro;
    }

    public void setRetiro(boolean retiro) {
        this.retiro = retiro;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public TipoPlanilla getTipoPlanilla() {
        return tipoPlanilla;
    }

    public void setTipoPlanilla(TipoPlanilla tipoPlanilla) {
        this.tipoPlanilla = tipoPlanilla;
    }

    public Long getNumeroFormulario() {
        return numeroFormulario;
    }

    public void setNumeroFormulario(Long numeroFormulario) {
        this.numeroFormulario = numeroFormulario;
    }

    public String getPlanilla() {
        return planilla;
    }

    public void setPlanilla(String planilla) {
        this.planilla = planilla;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public String getSubTipoCotizante() {
        return subTipoCotizante;
    }

    public void setSubTipoCotizante(String subTipoCotizante) {
        this.subTipoCotizante = subTipoCotizante;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Long consecutivo) {
        this.consecutivo = consecutivo;
    }

    public String getLey() {
        return ley;
    }

    public void setLey(String ley) {
        this.ley = ley;
    }
}