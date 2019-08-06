package com.sura.arl.reproceso.modelo.integrador;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaCambioActividad extends IntegradorEsperada {

    public static final String TIPO_AFILIADO_INDEPENDIENTE = "02";

    protected String poliza;

    protected String actividadAnterior;

    protected String actividadNueva;

    protected String dniAfiliado;

    protected String consecutivoIndependiente;

    protected String periodoInicial;

    protected String periodoFinal;

    protected double tasa;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getActividadAnterior() {
        return actividadAnterior;
    }

    public void setActividadAnterior(String actividadAnterior) {
        this.actividadAnterior = actividadAnterior;
    }

    public String getActividadNueva() {
        return actividadNueva;
    }

    public void setActividadNueva(String actividadNueva) {
        this.actividadNueva = actividadNueva;
    }

    public String getDniAfiliado() {
        return dniAfiliado;
    }

    public void setDniAfiliado(String dniAfiliado) {
        this.dniAfiliado = dniAfiliado;
    }

    public String getConsecutivoIndependiente() {
        return consecutivoIndependiente;
    }

    public void setConsecutivoIndependiente(String consecutivoIndependiente) {
        this.consecutivoIndependiente = consecutivoIndependiente;
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

    public double getTasa() {
        return tasa;
    }

    public void setTasa(double tasa) {
        this.tasa = tasa;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaCambioActividad{" + "poliza=" + poliza + ", actividadAnterior=" + actividadAnterior + ", actividadNueva=" + actividadNueva + ", dniAfiliado=" + dniAfiliado + ", consecutivoIndependiente=" + consecutivoIndependiente + ", periodoInicial=" + periodoInicial + ", periodFinal=" + periodoFinal + ", tasa=" + tasa + '}';
    }

}
