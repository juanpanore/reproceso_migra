package com.sura.arl.reproceso.modelo;

import java.util.List;

import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.EstadoPago;

public class ResultadoInexactitud {
    private Double saldo;
    private EstadoPago estadoPago;
    private List<ErrorProceso> errores;
    private boolean seActualizaEstadoCuenta;

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public List<ErrorProceso> getErrores() {
        return errores;
    }

    public void setErrores(List<ErrorProceso> errores) {
        this.errores = errores;
    }

    public boolean seActualizaEstadoCuenta() {
        return seActualizaEstadoCuenta;
    }

    public void setSeActualizaEstadoCuenta(boolean seActualizaEstadoCuenta) {
        this.seActualizaEstadoCuenta = seActualizaEstadoCuenta;
    }
}