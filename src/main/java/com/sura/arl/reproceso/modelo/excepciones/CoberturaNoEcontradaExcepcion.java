package com.sura.arl.reproceso.modelo.excepciones;

public class CoberturaNoEcontradaExcepcion extends RuntimeException {

    private static final long serialVersionUID = -3973623227645827425L;
    
    public CoberturaNoEcontradaExcepcion(){
        super();
    }
    
    public CoberturaNoEcontradaExcepcion(String mensaje){
        super(mensaje);
    }
}
