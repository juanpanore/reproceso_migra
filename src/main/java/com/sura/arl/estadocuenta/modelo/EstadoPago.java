package com.sura.arl.estadocuenta.modelo;

public enum EstadoPago {
    MORA_PRESUNTA("01"),
    AFILIADO_OK("02"), 
    ENRIQUES("03"), 
    DIFERENCIA_TASA("04"),
    DIFERENCIA_DIAS_COTIZACION("05");

    private String equivalencia;

    EstadoPago(String equivalencia) {
        this.setEquivalencia(equivalencia);
    }

    public String getEquivalencia() {
        return equivalencia;
    }

    public void setEquivalencia(String equivalencia) {
        this.equivalencia = equivalencia;
    }

    public static EstadoPago estadoPagoPorEquivalencia(String equivalencia) {
        for (EstadoPago t : EstadoPago.values()) {
            if (equivalencia.equals(t.getEquivalencia())) {
                return t;
            }
        }
        return null;
    }
}
