package com.sura.arl.reproceso.modelo.excepciones;

public class DatoIncorrectoExcepcion extends RuntimeException {

    private static final long serialVersionUID = 6875350244017132396L;


    public DatoIncorrectoExcepcion(String message) {
        super(message);
    }

}
