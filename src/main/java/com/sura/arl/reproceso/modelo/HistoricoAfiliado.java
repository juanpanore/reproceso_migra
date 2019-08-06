package com.sura.arl.reproceso.modelo;

import java.util.Date;

public class HistoricoAfiliado {
    private String poliza;
    private String dni;
    private String sucursal;
    private Date fealta;
    private Date febaja;
    private Long certificado;
    private String fuente;
    private String dniIngresa;
    private Date feIngresa;
    private String sucursalPagadora;
    private Date feModifica;
    private String dniModifica;
    private String tema;
    private Date feUltimaActualizacion;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
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

    public Long getCertificado() {
        return certificado;
    }

    public void setCertificado(Long certificado) {
        this.certificado = certificado;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDniIngresa() {
        return dniIngresa;
    }

    public void setDniIngresa(String dniIngresa) {
        this.dniIngresa = dniIngresa;
    }

    public Date getFeIngresa() {
        return feIngresa;
    }

    public void setFeIngresa(Date feIngresa) {
        this.feIngresa = feIngresa;
    }

    public String getSucursalPagadora() {
        return sucursalPagadora;
    }

    public void setSucursalPagadora(String sucursalPagadora) {
        this.sucursalPagadora = sucursalPagadora;
    }

    public Date getFeModifica() {
        return feModifica;
    }

    public void setFeModifica(Date feModifica) {
        this.feModifica = feModifica;
    }

    public String getDniModifica() {
        return dniModifica;
    }

    public void setDniModifica(String dniModifica) {
        this.dniModifica = dniModifica;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public Date getFeUltimaActualizacion() {
        return feUltimaActualizacion;
    }

    public void setFeUltimaActualizacion(Date feUltimaActualizacion) {
        this.feUltimaActualizacion = feUltimaActualizacion;
    }

    public HistoricoAfiliado() {
        super();
    }

    @Override
    public String toString() {
        return "HistoricoAfiliado [poliza=" + poliza + ", dni=" + dni + ", sucursal=" + sucursal + ", fealta=" + fealta
                + ", febaja=" + febaja + ", certificado=" + certificado + ", fuente=" + fuente + ", dniIngresa="
                + dniIngresa + ", feIngresa=" + feIngresa + ", sucursalPagadora=" + sucursalPagadora + ", feModifica="
                + feModifica + ", dniModifica=" + dniModifica + ", tema=" + tema + ", feUltimaActualizacion="
                + feUltimaActualizacion + "]";
    }
}
