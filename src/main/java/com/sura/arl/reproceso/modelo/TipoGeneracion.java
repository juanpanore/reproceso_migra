package com.sura.arl.reproceso.modelo;

public enum TipoGeneracion {

    ANTICIPADA("A"), VENCIDA("V");
    
    private String equivalencia;

    TipoGeneracion(String equivalencia) {
        this.setEquivalencia(equivalencia);
    }

    public String getEquivalencia() {
        return equivalencia;
    }
    
    public void setEquivalencia(String equivalencia) {
        this.equivalencia = equivalencia;
    }
    
}
