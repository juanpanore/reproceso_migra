package com.sura.arl.reproceso.modelo;

import java.util.Date;
import java.util.Objects;

public class DetallePago {

    private String npoliza;
    private String dni;
    private Date fechaPago;
    private TipoPlanilla tipoPlanilla;
    private String periodo;
    private Long numeroFormulario;
    private String responsable;
    private String tipoCotizante;
    private String subTipoCotizante;
    private String planilla;

    private Double ibcAusentismo;
    private Double diasAusentismo;
    private Double salarioAusentismo;
    private Double tasaAusentismo;
    private Double cotizacionAusentismo;
    private Double ibcLaborado;
    private Double diasLaborados;
    private Double salarioLaborado;
    private Double tasaLaborado;
    private Double cotizacionLaborado;
    private String idNovedad;
    
    private Long consecutivoEstadoCuenta;

    private boolean tieneIngreso;
    private boolean tieneRetiro;
    
    public String getIdNovedad() {
        return idNovedad;
    }

    public void setIdNovedad(String idNovedad) {
        this.idNovedad = idNovedad;
    }

    public String getNpoliza() {
        return npoliza;
    }

    public void setNpoliza(String npoliza) {
        this.npoliza = npoliza;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Long getNumeroFormulario() {
        return numeroFormulario;
    }

    public void setNumeroFormulario(Long numeroFormulario) {
        this.numeroFormulario = numeroFormulario;
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

    public String getPlanilla() {
        return planilla;
    }

    public void setPlanilla(String planilla) {
        this.planilla = planilla;
    }

    public Double getIbcAusentismo() {
        return ibcAusentismo;
    }

    public void setIbcAusentismo(Double ibcAusentismo) {
        this.ibcAusentismo = ibcAusentismo;
    }

    public Double getDiasAusentismo() {
        return diasAusentismo;
    }

    public void setDiasAusentismo(Double diasAusentismo) {
        this.diasAusentismo = diasAusentismo;
    }

    public Double getSalarioAusentismo() {
        return salarioAusentismo;
    }

    public void setSalarioAusentismo(Double salarioAusentismo) {
        this.salarioAusentismo = salarioAusentismo;
    }

    public Double getTasaAusentismo() {
        return tasaAusentismo;
    }

    public void setTasaAusentismo(Double tasaAusentismo) {
        this.tasaAusentismo = tasaAusentismo;
    }

    public Double getCotizacionAusentismo() {
        return cotizacionAusentismo;
    }

    public void setCotizacionAusentismo(Double cotizacionAusentismo) {
        this.cotizacionAusentismo = cotizacionAusentismo;
    }

    public Double getIbcLaborado() {
        return ibcLaborado;
    }

    public void setIbcLaborado(Double ibcLaborado) {
        this.ibcLaborado = ibcLaborado;
    }

    public Double getDiasLaborados() {
        return diasLaborados;
    }

    public void setDiasLaborados(Double diasLaborados) {
        this.diasLaborados = diasLaborados;
    }

    public Double getSalarioLaborado() {
        return salarioLaborado;
    }

    public void setSalarioLaborado(Double salarioLaborado) {
        this.salarioLaborado = salarioLaborado;
    }

    public Double getTasaLaborado() {
        return tasaLaborado;
    }

    public void setTasaLaborado(Double tasaLaborado) {
        this.tasaLaborado = tasaLaborado;
    }

    public Double getCotizacionLaborado() {
        return cotizacionLaborado;
    }

    public void setCotizacionLaborado(Double cotizacionLaborado) {
        this.cotizacionLaborado = cotizacionLaborado;
    }

    public String getSubTipoCotizante() {
        return subTipoCotizante;
    }

    public void setSubTipoCotizante(String subTipoCotizante) {
        this.subTipoCotizante = subTipoCotizante;
    }

    public boolean tieneIngreso() {
        return tieneIngreso;
    }

    public void setTieneIngreso(boolean tieneIngreso) {
        this.tieneIngreso = tieneIngreso;
    }

    public boolean tieneRetiro() {
        return tieneRetiro;
    }

    public void setTieneRetiro(boolean tieneRetiro) {
        this.tieneRetiro = tieneRetiro;
    }

    public Long getConsecutivoEstadoCuenta() {
        return consecutivoEstadoCuenta;
    }

    public void setConsecutivoEstadoCuenta(Long consecutivoEstadoCuenta) {
        this.consecutivoEstadoCuenta = consecutivoEstadoCuenta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cotizacionAusentismo, cotizacionLaborado, diasAusentismo, diasLaborados, dni, fechaPago,
                ibcAusentismo, ibcLaborado, npoliza, numeroFormulario, periodo, planilla, responsable,
                salarioAusentismo, salarioLaborado, subTipoCotizante, tasaAusentismo, tasaLaborado, tieneIngreso,
                tieneRetiro, tipoCotizante, tipoPlanilla);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetallePago other = (DetallePago) obj;
        return Objects.equals(dni, other.dni) && Objects.equals(fechaPago, other.fechaPago)
                && Objects.equals(npoliza, other.npoliza) 
                && Objects.equals(numeroFormulario, other.numeroFormulario)
                && Objects.equals(periodo, other.periodo)
                && Objects.equals(subTipoCotizante, other.subTipoCotizante)
                && Objects.equals(tipoCotizante, other.tipoCotizante) 
                && tipoPlanilla == other.tipoPlanilla;
    }

    @Override
    public String toString() {
        return "DetallePago [npoliza=" + npoliza + ", dni=" + dni + ", fechaPago=" + fechaPago + ", tipoPlanilla="
                + tipoPlanilla + ", periodo=" + periodo + ", numeroFormulario=" + numeroFormulario + ", responsable="
                + responsable + ", tipoCotizante=" + tipoCotizante + ", subTipoCotizante=" + subTipoCotizante
                + ", planilla=" + planilla + ", ibcAusentismo=" + ibcAusentismo + ", diasAusentismo=" + diasAusentismo
                + ", salarioAusentismo=" + salarioAusentismo + ", tasaAusentismo=" + tasaAusentismo
                + ", cotizacionAusentismo=" + cotizacionAusentismo + ", ibcLaborado=" + ibcLaborado + ", diasLaborados="
                + diasLaborados + ", salarioLaborado=" + salarioLaborado + ", tasaLaborado=" + tasaLaborado
                + ", cotizacionLaborado=" + cotizacionLaborado + ", tieneIngreso=" + tieneIngreso + ", tieneRetiro="
                + tieneRetiro + "]";
    }

    public DetallePago(String npoliza, String dni, Date fechaPago, TipoPlanilla tipoPlanilla, String periodo,
            Long numeroFormulario, String responsable, String tipoCotizante, String planilla, String subTipoCotizante) {
        super();
        this.npoliza = npoliza;
        this.dni = dni;
        this.fechaPago = fechaPago;
        this.tipoPlanilla = tipoPlanilla;
        this.periodo = periodo;
        this.numeroFormulario = numeroFormulario;
        this.responsable = responsable;
        this.tipoCotizante = tipoCotizante;
        this.planilla = planilla;
        this.subTipoCotizante = subTipoCotizante;

        this.ibcAusentismo = 0D;
        this.diasAusentismo = 0D;
        this.salarioAusentismo = 0D;
        this.tasaAusentismo = 0D;
        this.cotizacionAusentismo = 0D;
        this.ibcLaborado = 0D;
        this.diasLaborados = 0D;
        this.salarioLaborado = 0D;
        this.tasaLaborado = 0D;
        this.cotizacionLaborado = 0D;

        this.tieneIngreso = false;
        this.tieneRetiro = false;
    }

}
