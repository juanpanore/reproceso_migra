package com.sura.arl.reproceso.modelo.excepciones;

public class ForzarReprocesoExcepcion extends RuntimeException {
    private static final long serialVersionUID = 3121381383393533627L;
    private String dni;
    private String periodo;
    private String poliza;

    public ForzarReprocesoExcepcion(String message) {
        super(message);
    }

    public ForzarReprocesoExcepcion(String dni, String periodo, String poliza) {
        this.dni = dni;
        this.periodo = periodo;
        this.poliza = poliza;
    }

    public String getDni() {
        return dni;
    }

    public String getPeriodo() {
        return periodo;
    }

    public String getPoliza() {
        return poliza;
    }

}
