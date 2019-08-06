package com.sura.arl.reproceso.modelo;

public final class ReprocesoCompletado {
    final String poliza;
    final String dni;
    final String dniProcesa;
    final String periodo;
    final Long numeroFormulario;

    public ReprocesoCompletado(String poliza, String dni, String dniProcesa, String periodo, Long numeroFormulario) {
        super();
        this.poliza = poliza;
        this.dni = dni;
        this.dniProcesa = dniProcesa;
        this.periodo = periodo;
        this.numeroFormulario = numeroFormulario;
    }

    public String getPoliza() {
        return poliza;
    }

    public String getDni() {
        return dni;
    }

    public String getDniProcesa() {
        return dniProcesa;
    }

    public String getPeriodo() {
        return periodo;
    }

    public Long getNumeroFormulario() {
        return numeroFormulario;
    }

}
