package com.sura.arl.reproceso.modelo.integrador;

import java.util.StringJoiner;

public class IntegradorEsperadaReversaLegalizacion extends IntegradorEsperada {
    
    private String poliza;
    private String periodo;
    private String nmformulario;
    private String dniEmpleador;
    
    public String getPoliza() {
        return poliza;
    }
    public String getPeriodo() {
        return periodo;
    }
    public String getNmformulario() {
        return nmformulario;
    }
    public String getDniEmpleador() {
        return dniEmpleador;
    }
    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }
    public void setNmformulario(String nmformulario) {
        this.nmformulario = nmformulario;
    }
    public void setDniEmpleador(String dniEmpleador) {
        this.dniEmpleador = dniEmpleador;
    }
    
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("poliza=" + poliza);
        joiner.add("periodo=" + periodo);
        joiner.add("dniEmpleador=" + dniEmpleador);
        joiner.add("nmformulario=" + nmformulario);
        
        return joiner.toString();
    }
}
