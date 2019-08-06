package com.sura.arl.reproceso.modelo;

public enum EstadoTrazaAfectacionSaldo implements Equivalencia{
    INICIAL("01"), RECALCULADO("02"), ASIGNADO("03"), ANULADO("04"), PAGADO("05"), REVERSA_DE_LEGALIZACION("06"), DEVOLUCION("07"), SOLUCIONADO("08");

    private String equivalencia;

    EstadoTrazaAfectacionSaldo(String equivalencia) {
        this.equivalencia = equivalencia;
    }

    @Override
    public String getEquivalencia() {
        return this.equivalencia;
    }
}
