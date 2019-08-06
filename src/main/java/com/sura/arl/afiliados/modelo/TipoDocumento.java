package com.sura.arl.afiliados.modelo;

/**
 * Enumeracion que contiene los tipos de documentos de personas u empresas que
 * maneja el sistema.
 *
 */
public enum TipoDocumento {
    CC("C"), NI("N"), CE("E"), TI("T"), RC("I"), M("M"), PA("P"), CD("D"), SC("S"), CN("V"), NU("U"), PE("Z");

    private String equivalencia;

    TipoDocumento(String equivalencia) {
        this.setEquivalencia(equivalencia);
    }

    public String getEquivalencia() {
        return equivalencia;
    }

    public void setEquivalencia(String equivalencia) {
        this.equivalencia = equivalencia;
    }

    
    public static TipoDocumento tipoDocumentoPorEquivalencia(String equivalencia) {
        for (TipoDocumento t : TipoDocumento.values()) {
            if (equivalencia.equals(t.getEquivalencia())) {
                return t;
            }
        }
        return null;
    }
}