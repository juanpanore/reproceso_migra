package com.sura.arl.reproceso.modelo.integrador;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaCentroTrabajo extends IntegradorEsperadaAfiliado{
    
    protected String centroTrabajo;
    
    protected String centroTrabajoAnterior;
    
    protected String periodoInicial;
    
    protected String periodoFinal;

    public String getCentroTrabajo() {
        return centroTrabajo;
    }

    public void setCentroTrabajo(String centroTrabajo) {
        this.centroTrabajo = centroTrabajo;
    }

    public String getCentroTrabajoAnterior() {
        return centroTrabajoAnterior;
    }

    public void setCentroTrabajoAnterior(String centroTrabajoAnterior) {
        this.centroTrabajoAnterior = centroTrabajoAnterior;
    }

    public String getPeriodoInicial() {
        return periodoInicial;
    }

    public void setPeriodoInicial(String periodoInicial) {
        this.periodoInicial = periodoInicial;
    }

    public String getPeriodoFinal() {
        return periodoFinal;
    }

    public void setPeriodoFinal(String periodoFinal) {
        this.periodoFinal = periodoFinal;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaCentroTrabajo{" + "centroTrabajo=" + centroTrabajo + ", centroTrabajoAnterior=" + centroTrabajoAnterior + ", periodoInicial=" + periodoInicial + ", periodoFinal=" + periodoFinal + '}';
    }
    
}
