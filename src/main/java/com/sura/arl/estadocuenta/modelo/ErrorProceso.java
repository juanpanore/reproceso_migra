package com.sura.arl.estadocuenta.modelo;

import java.util.Date;

public class ErrorProceso {

    protected String codError;

    protected Date fechaRegistro;

    protected String npoliza;

    protected String periodo;

    protected String dni;

    protected String usuarioRegistro;

    protected String periodoGeneracion;

    protected String codigoProceso;

    protected String tipoGeneracion;

    protected String observacion;

    protected EstadoError estadoError;

    protected String tipoCotizante;

    public String getCodError() {
        return codError;
    }

    public void setCodError(String codError) {
        this.codError = codError;
    }

    public Date getFechaRegistro() {
        if (fechaRegistro != null) {
            return new Date(fechaRegistro.getTime());
        }
        return null;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        if (fechaRegistro != null) {
            this.fechaRegistro = new Date(fechaRegistro.getTime());
        } else {
            this.fechaRegistro = null;
        }
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public String getNpoliza() {
        return npoliza;
    }

    public void setNpoliza(String npoliza) {
        this.npoliza = npoliza;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPeriodoGeneracion() {
        return periodoGeneracion;
    }

    public void setPeriodoGeneracion(String periodoGeneracion) {
        this.periodoGeneracion = periodoGeneracion;
    }

    public String getCodigoProceso() {
        return codigoProceso;
    }

    public void setCodigoProceso(String codigoProceso) {
        this.codigoProceso = codigoProceso;
    }

    public String getTipoGeneracion() {
        return tipoGeneracion;
    }

    public void setTipoGeneracion(String tipoGeneracion) {
        this.tipoGeneracion = tipoGeneracion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public EstadoError getEstadoError() {
        return estadoError;
    }

    public void setEstadoError(EstadoError estadoError) {
        this.estadoError = estadoError;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public ErrorProceso(String codError, Date fechaRegistro, String npoliza, String periodo, String dni,
            String usuarioRegistro, String periodoGeneracion, String codigoProceso, String observacion,
            String tipoGeneracion, String tipoCotizante, EstadoError estadoError) {
        super();
        this.codError = codError;
        this.fechaRegistro = fechaRegistro;
        this.npoliza = npoliza;
        this.periodo = periodo;
        this.dni = dni;
        this.usuarioRegistro = usuarioRegistro;
        this.periodoGeneracion = periodoGeneracion;
        this.codigoProceso = codigoProceso;
        this.observacion = observacion;
        this.tipoGeneracion = tipoGeneracion;
        this.estadoError = estadoError;
        this.tipoCotizante = tipoCotizante;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String codError;
        private Date fechaRegistro;
        private String npoliza;
        private String periodo;
        private String dni;
        private String usuarioRegistro;
        private String periodoGeneracion;
        private String codigoProceso;
        private String tipoGeneracion;
        private String observacion;
        private EstadoError estadoError;
        private String tipoCotizante;

        public Builder dni(String valor) {
            this.dni = valor;
            return this;
        }

        public Builder periodo(String valor) {
            this.periodo = valor;
            return this;
        }

        public Builder codError(String valor) {
            this.codError = valor;
            return this;
        }

        public Builder fechaRegistro(Date valor) {
            this.fechaRegistro = valor;
            return this;
        }

        public Builder usuarioRegistro(String valor) {
            this.usuarioRegistro = valor;
            return this;
        }

        public Builder periodoGeneracion(String valor) {
            this.periodoGeneracion = valor;
            return this;
        }

        public Builder codigoProceso(String valor) {
            this.codigoProceso = valor;
            return this;
        }

        public Builder tipoGeneracion(String valor) {
            this.tipoGeneracion = valor;
            return this;
        }

        public Builder npoliza(String valor) {
            this.npoliza = valor;
            return this;
        }

        public Builder observacion(String valor) {
            this.observacion = valor;
            return this;
        }

        public Builder tipoCotizante(String valor) {
            this.tipoCotizante = valor;
            return this;
        }

        public Builder estadoError(EstadoError valor) {
            this.estadoError = valor;
            return this;
        }

        public ErrorProceso build() {
            return new ErrorProceso(codError, fechaRegistro, npoliza, periodo, dni, usuarioRegistro, periodoGeneracion,
                    codigoProceso, observacion, tipoGeneracion, tipoCotizante, estadoError);
        }
    }

    public enum EstadoError {

        POR_CORREGIR, GESTIONADO, CORREGIDO

    }
}