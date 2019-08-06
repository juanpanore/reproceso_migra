package com.sura.arl.reproceso.modelo.integrador;

import java.util.Date;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaAfiliado extends IntegradorEsperada {

    protected String dniAfiliado;

    protected String poliza;

    protected String tipoAfiliado;

    protected String tipoCotizante;

    protected String certificado;

    protected Date fechaAlta;

    protected Date fechaBaja;
    
    protected String periodo;
    
    protected String subtipo;

    protected String formularioPago;
    
    protected String dniEmpleador;
    
    protected String rowId;

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

    public String getTipoAfiliado() {
        return tipoAfiliado;
    }

    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public String getCertificado() {
        return certificado;
    }

    public void setCertificado(String certificado) {
        this.certificado = certificado;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getSubtipo() {
        return subtipo;
    }

    public void setSubtipo(String subtipo) {
        this.subtipo = subtipo;
    }

    public String getFormularioPago() {
        return formularioPago;
    }

    public void setFormularioPago(String formularioPago) {
        this.formularioPago = formularioPago;
    }

    public String getDniEmpleador() {
        return dniEmpleador;
    }

    public void setDniEmpleador(String dniEmpleador) {
        this.dniEmpleador = dniEmpleador;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaAfiliado{" + "dniAfiliado=" + dniAfiliado + ", poliza=" + poliza + ", tipoAfiliado=" + tipoAfiliado + ", tipoCotizante=" + tipoCotizante + ", certificado=" + certificado + ", fechaAlta=" + fechaAlta + ", fechaBaja=" + fechaBaja + ", periodo=" + periodo + ", subtipo=" + subtipo + ", formularioPago=" + formularioPago + ", dniEmpleador=" + dniEmpleador + ", rowId=" + rowId + '}';
    }

}
