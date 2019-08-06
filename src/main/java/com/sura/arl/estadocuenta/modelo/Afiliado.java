package com.sura.arl.estadocuenta.modelo;

import java.util.Date;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;

public class Afiliado {

    private String dni;
    private String tipoAfiliado;
    private String tipoCotizante;
    private String subTipoCotizante;
    private Cobertura cobertura;
    private Integer salario;
    private Condicion condicion;
    private String tipoError;
    private Double ultimoIbc;
    private String certificado;
    private int nmroCoberturas;
    private Date fechaLimitePago;

	public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTipoAfiliado() {
        return tipoAfiliado;
    }

    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

	public String getSubTipoCotizante() {
        return subTipoCotizante;
    }

    public void setSubTipoCotizante(String subTipoCotizante) {
        this.subTipoCotizante = subTipoCotizante;
    }

    public Cobertura getCobertura() {
        return cobertura;
    }

    public void setCobertura(Cobertura cobertura) {
        this.cobertura = cobertura;
    }

    public Integer getSalario() {
        return salario;
    }

    public void setSalario(Integer salario) {
        this.salario = salario;
    }

    public Condicion getCondicion() {
        return condicion;
    }

    public void setCondicion(Condicion condicion) {
        this.condicion = condicion;
    }

    public String getTipoError() {
        return tipoError;
    }

    public void setTipoError(String tipoError) {
        this.tipoError = tipoError;
    }

    public Double getUltimoIbc() {
        return ultimoIbc;
    }

    public void setUltimoIbc(Double ultimoIbc) {
        this.ultimoIbc = ultimoIbc;
    }

    public String getCertificado() {
        return certificado;
    }

    public void setCertificado(String certificado) {
        this.certificado = certificado;
    }
    
    public int getNmroCoberturas() {
		return nmroCoberturas;
	}

	public void setNmroCoberturas(int nmroCoberturas) {
		this.nmroCoberturas = nmroCoberturas;
	}
	
    public Date getFechaLimitePago() {
		return fechaLimitePago;
	}

	public void setFechaLimitePago(Date fechaLimitePago) {
		this.fechaLimitePago = fechaLimitePago;
	}

    public enum TipoAfiliado {

        EMPRESA("01"), INDEPENDIENTE("02");

        private String equivalencia;

        TipoAfiliado(String equivalencia) {
            this.equivalencia = equivalencia;
        }

        public String getEquivalencia() {
            return this.equivalencia;
        }
    }
    
    public enum TipoCotizante {

        ESTUDIANTE("23");

        private String equivalencia;

        TipoCotizante(String equivalencia) {
            this.equivalencia = equivalencia;
        }

        public String getEquivalencia() {
            return this.equivalencia;
        }
    }
}
