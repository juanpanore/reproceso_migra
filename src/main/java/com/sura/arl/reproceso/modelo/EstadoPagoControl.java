package com.sura.arl.reproceso.modelo;

public enum EstadoPagoControl {

    MORA_PRESUNTA_TOTAL("01"), MORA_TOTAL("02"), MORA_PARCIAL("03"), INEXACTITUD_PAGOS("04"), AL_DIA("05");

    private String equivalencia;

    EstadoPagoControl(String equivalencia) {
        this.equivalencia = equivalencia;
    }

    public String getEquivalencia() {
        return this.equivalencia;
    }
}
