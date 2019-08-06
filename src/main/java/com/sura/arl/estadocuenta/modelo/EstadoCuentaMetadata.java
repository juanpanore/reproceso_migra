package com.sura.arl.estadocuenta.modelo;

import java.util.Date;

public class EstadoCuentaMetadata {
    
    public static final String CAMPO_NMFORMULARIO_PAGO = "NUMERO_FORMULARIO_PAGO";

    private Long nmconsecutivo;
    private String poliza;
    private String periodo;
    private String dniAfiliado;
    private String tipoCotizante;
    private String campo;
    private String valor;
    private Date feIngresa;
    private String dniIngresa;

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

    public String getDniAfiliado() {
        return dniAfiliado;
    }

    public void setDniAfiliado(String dniAfiliado) {
        this.dniAfiliado = dniAfiliado;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Date getFeIngresa() {
        return feIngresa;
    }

    public void setFeIngresa(Date feIngresa) {
        this.feIngresa = feIngresa;
    }

    public String getDniIngresa() {
        return dniIngresa;
    }

    public void setDniIngresa(String dniIngresa) {
        this.dniIngresa = dniIngresa;
    }

    public Long getNmconsecutivo() {
        return nmconsecutivo;
    }

    public void setNmconsecutivo(Long nmconsecutivo) {
        this.nmconsecutivo = nmconsecutivo;
    }

    @Override
    public String toString() {
        return "EstadoCuentaMetadata [poliza=" + poliza + ", periodo=" + periodo + ", dniAfiliado=" + dniAfiliado
                + ", tipoCotizante=" + tipoCotizante + ", feingresa=" + feIngresa
                + ", idUsuario=" + dniIngresa + ", campo=" + campo + ", valor=" + valor + "]";
    }
}
