package com.sura.arl.reproceso.util;

public enum TipoConsultaFechaHabil {
    MIN("MIN"), MAX("MAX"), ALL("ALL"), INT("INT");
    public String tipoConsulta;

    private TipoConsultaFechaHabil(String tipoConsulta) {
        this.tipoConsulta = tipoConsulta;
    }

    public String getTipoConsulta() {
        return tipoConsulta;
    }

    public void setTipoConsulta(String tipoConsulta) {
        this.tipoConsulta = tipoConsulta;
    }
}
