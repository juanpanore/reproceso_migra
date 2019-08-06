package com.sura.arl.reproceso.modelo;

import com.sura.arl.reproceso.accesodatos.HistoricoInconsistenciaCambioCTDao.InconsistenciaCambioCT;

public class HistoricoInconsistenciaCambioCT {
    private String poliza;
    private String dni;
    private String nmperiodo;
    private String sucursalAnterior;
    private String sucursalNueva;
    private InconsistenciaCambioCT codigoInconsistencia;
    private String dniIngresa;
    private boolean cambio;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDniIngresa() {
        return dniIngresa;
    }

    public void setDniIngresa(String dniIngresa) {
        this.dniIngresa = dniIngresa;
    }

    public String getSucursalAnterior() {
        return sucursalAnterior;
    }

    public void setSucursalAnterior(String sucursalAnterior) {
        this.sucursalAnterior = sucursalAnterior;
    }

    public String getSucursalNueva() {
        return sucursalNueva;
    }

    public void setSucursalNueva(String sucursalNueva) {
        this.sucursalNueva = sucursalNueva;
    }

    public String getNmperiodo() {
        return nmperiodo;
    }

    public void setNmperiodo(String nmperiodo) {
        this.nmperiodo = nmperiodo;
    }

    public InconsistenciaCambioCT getCodigoInconsistencia() {
        return codigoInconsistencia;
    }

    public void setCodigoInconsistencia(InconsistenciaCambioCT codigoInconsistencia) {
        this.codigoInconsistencia = codigoInconsistencia;
    }

    public boolean seCambia() {
        return cambio;
    }

    public void setCambio(boolean cambio) {
        this.cambio = cambio;
    }

    public HistoricoInconsistenciaCambioCT() {
        super();
    }

}
