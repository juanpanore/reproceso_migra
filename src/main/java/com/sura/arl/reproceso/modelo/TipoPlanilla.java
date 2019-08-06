package com.sura.arl.reproceso.modelo;


public enum TipoPlanilla {
    E("E"), Y("Y"), A("A"), I("I"), S("S"), M("M"), N("N"), T("T"), F("F"), K("K"), J("J"), X("X"), U("U"), H("H"), $("S");

    private String equivalencia;
    
    TipoPlanilla(String equivalencia){
    	this.setEquivalencia(equivalencia);
    }
    public String getEquivalencia() {
        return equivalencia;
    }

    public void setEquivalencia(String equivalencia) {
        this.equivalencia = equivalencia;
    }
    
    public static boolean contiene(String valor){
        
        for(TipoPlanilla tipoPlanilla : TipoPlanilla.values()){
            if(tipoPlanilla.name().equals(valor)){
                return true;
            }
        }
        
        return false;
    }
    
    public static TipoPlanilla tipoPlanillaPorEquivalencia(String equivalencia) {
        for (TipoPlanilla t : TipoPlanilla.values()) {
            if (equivalencia.equals(t.getEquivalencia())) {
                return t;
            }
        }
        return null;
    }
}
