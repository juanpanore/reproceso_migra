package com.sura.arl.reproceso.modelo.excepciones;

public class FechaLimitePagoExcepcion extends RuntimeException {

    private static final long serialVersionUID = -3973623227645827425L;

    public FechaLimitePagoExcepcion(String msg) {
        super(msg);
    }

    public FechaLimitePagoExcepcion(String msg, Throwable cause) {
        super(msg, cause);
    }

}
