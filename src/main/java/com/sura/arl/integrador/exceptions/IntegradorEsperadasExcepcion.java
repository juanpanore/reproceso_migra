package com.sura.arl.integrador.exceptions;

public class IntegradorEsperadasExcepcion extends RuntimeException {

    private static final long serialVersionUID = 5890322385033044084L;

    public IntegradorEsperadasExcepcion() {
        super();
    }

    public IntegradorEsperadasExcepcion(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegradorEsperadasExcepcion(String message) {
        super(message);
    }

}
