package com.sura.arl.reproceso.modelo.integrador;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaCambioTasa extends IntegradorEsperada {

    protected String poliza;

    protected String centroTrabajo;

    protected String actividad;

    protected String clase;
    
    protected String periodoInicial;
    
    protected String periodoFinal;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getCentroTrabajo() {
        return centroTrabajo;
    }

    public void setCentroTrabajo(String centroTrabajo) {
        this.centroTrabajo = centroTrabajo;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
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
        return "IntegradorEsperadaCambioTasa{" + "poliza=" + poliza + ", sucursal=" + centroTrabajo + ", actividad=" + actividad + ", clase=" + clase + ", periodoInicial=" + periodoInicial + ", periodoFinal=" + periodoFinal + '}';
    }

}
