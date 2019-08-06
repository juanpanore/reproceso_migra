package com.sura.arl.reproceso.modelo.integrador;

import java.util.Date;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaCobertura extends IntegradorEsperadaAfiliado {

    private Date fechaAltaAnterior;

    private Date fechaBajaAnterior;

    public Date getFechaAltaAnterior() {
        return fechaAltaAnterior;
    }

    public void setFechaAltaAnterior(Date fechaAltaAnterior) {
        this.fechaAltaAnterior = fechaAltaAnterior;
    }

    public Date getFechaBajaAnterior() {
        return fechaBajaAnterior;
    }

    public void setFechaBajaAnterior(Date fechaBajaAnterior) {
        this.fechaBajaAnterior = fechaBajaAnterior;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaCobertura{" + "dniAfiliado=" + dniAfiliado + ", poliza=" + poliza + ", certificado=" + certificado + ", fechaAlta=" + fechaAlta + ", fechaBaja=" + fechaBaja + ", fechaAltaAnterior=" + fechaAltaAnterior + ", fechaBajaAnterior=" + fechaBajaAnterior + '}';
    }

}
