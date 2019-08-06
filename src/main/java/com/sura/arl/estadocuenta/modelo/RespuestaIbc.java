package com.sura.arl.estadocuenta.modelo;

import java.util.Optional;

public class RespuestaIbc {
    private Double ibc;
    private Optional<Double> salario;
    private Optional<String> observaciones;

    public Double getIbc() {
        return ibc;
    }

    public void setIbc(Double ibc) {
        this.ibc = ibc;
    }

    public Optional<Double> getSalario() {
        return salario;
    }

    public void setSalario(Optional<Double> salario) {
        this.salario = salario;
    }

    public Optional<String> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(Optional<String> observaciones) {
        this.observaciones = observaciones;
    }

    public RespuestaIbc(Double ibc, Optional<Double> salario, Optional<String> observaciones) {
        super();
        this.ibc = ibc;
        this.salario = salario;
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "RespuestaIbc [ibc=" + ibc + ", salario=" + salario + ", observaciones=" + observaciones + "]";
    }

}
