package com.sura.arl.integrador.modelo;

import com.sura.arl.reproceso.util.Periodo;
import java.util.Date;

/**
 *
 * @author pragma.co
 */
public class ControlIntegrador {
    
    private Long consecutivo;
    
    private Periodo periodo;
    
    private String contrato;
    
    private CambioIntegrador motivoCambio;
    
    private Date fechaIngreso;
    
    private String dniIngreso;

    public Long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Long consecutivo) {
        this.consecutivo = consecutivo;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        this.periodo = periodo;
    }

    public String getContrato() {
        return contrato;
    }

    public void setContrato(String contrato) {
        this.contrato = contrato;
    }

    public CambioIntegrador getMotivoCambio() {
        return motivoCambio;
    }

    public void setMotivoCambio(CambioIntegrador motivoCambio) {
        this.motivoCambio = motivoCambio;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getDniIngreso() {
        return dniIngreso;
    }

    public void setDniIngreso(String dniIngreso) {
        this.dniIngreso = dniIngreso;
    }
 
}
