package com.sura.arl.afiliados.modelo;

import java.util.Date;

import com.sura.arl.reproceso.modelo.InfoNovedadVCT;

public class Afiliado {

    private String dni;
    private String tipoAfiliado; // tipo de afiliado desde bd
    private String tipoAfiliadoEstadoCuenta; // tipo de afil "calculado", q aplica solo para el estado de cuenta
    private String tipoCotizante;
    private String subtipoCotizante;
    private Integer salario;
    private String tipoError;
    private Double ultimoIbc;
    private String certificado;
    private int nmroCoberturas;

    private Date fechaLimitePago;
    private String periodoCotizacion;
    private Date fechaInicioNovedad;
    private Date fechaFinNovedad;
    private String tienePago;
    private String dniEmpleador;
    private boolean esIndependiente;
    private boolean esEstudiante;
    private boolean tieneNovedadIngreso;
    private boolean tieneNovedadRetiro;

    private TipoDocumento tipoDocumentoEmpleador;

    private Cobertura cobertura;
    private Condicion condicion;
    private Legalizacion legalizacion;
    private InfoNovedadVCT infoVct;
    
    private String usuarioOperacion;
    private String csvTiposCotizantes;

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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Cobertura getCobertura() {
        return cobertura;
    }

    public void setCobertura(Cobertura cobertura) {
        this.cobertura = cobertura;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public String getTienePago() {
        return tienePago;
    }

    public void setTienePago(String tienePago) {
        this.tienePago = tienePago;
    }

    public String getPeriodoCotizacion() {
        return periodoCotizacion;
    }

    public void setPeriodoCotizacion(String periodoCotizacion) {
        this.periodoCotizacion = periodoCotizacion;
    }

    public String getNumeroDocumento() {
        if (dni != null) {
            return dni.substring(1);
        }
        return null;
    }

    public boolean tieneNovedadIngreso() {
        return tieneNovedadIngreso;
    }

    public void setTieneNovedadIngreso(boolean tieneNovedadIngreso) {
        this.tieneNovedadIngreso = tieneNovedadIngreso;
    }

    public boolean tieneNovedadRetiro() {
        return tieneNovedadRetiro;
    }

    public void setTieneNovedadRetiro(boolean tieneNovedadRetiro) {
        this.tieneNovedadRetiro = tieneNovedadRetiro;
    }

    public Date getFechaInicioNovedad() {
        return fechaInicioNovedad;
    }

    public void setFechaInicioNovedad(Date fechaInicioNovedad) {
        this.fechaInicioNovedad = fechaInicioNovedad;
    }

    public Date getFechaFinNovedad() {
        return fechaFinNovedad;
    }

    public void setFechaFinNovedad(Date fechaFinNovedad) {
        this.fechaFinNovedad = fechaFinNovedad;
    }

    public boolean isTieneNovedadIngreso() {
        return tieneNovedadIngreso;
    }

    public boolean isTieneNovedadRetiro() {
        return tieneNovedadRetiro;
    }

    public String getTipoAfiliado() {
        return tipoAfiliado;
    }

    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
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

    public TipoDocumento getTipoDocumentoEmpleador() {
        return tipoDocumentoEmpleador;
    }

    public void setTipoDocumentoEmpleador(TipoDocumento tipoDocumentoEmpleador) {
        this.tipoDocumentoEmpleador = tipoDocumentoEmpleador;
    }

    public String getDniEmpleador() {
        return dniEmpleador;
    }

    public void setDniEmpleador(String dniEmpleador) {
        this.dniEmpleador = dniEmpleador;
    }

    public String getCertificado() {
        return certificado;
    }

    public void setCertificado(String certificado) {
        this.certificado = certificado;
    }

    public Legalizacion getLegalizacion() {
        return legalizacion;
    }

    public void setLegalizacion(Legalizacion legalizacion) {
        this.legalizacion = legalizacion;
    }

    public String getSubtipoCotizante() {
        return subtipoCotizante;
    }

    public void setSubtipoCotizante(String subtipoCotizante) {
        this.subtipoCotizante = subtipoCotizante;
    }

    public boolean esIndependiente() {
        return esIndependiente;
    }

    public void setEsIndependiente(boolean esIndependiente) {
        this.esIndependiente = esIndependiente;
    }

    public boolean esEstudiante() {
        return esEstudiante;
    }

    public void setEsEstudiante(boolean esEstudiante) {
        this.esEstudiante = esEstudiante;
    }

    public String getTipoAfiliadoEstadoCuenta() {
        return tipoAfiliadoEstadoCuenta;
    }

    public void setTipoAfiliadoEstadoCuenta(String tipoAfiliadoEstadoCuenta) {
        this.tipoAfiliadoEstadoCuenta = tipoAfiliadoEstadoCuenta;
    }

    public InfoNovedadVCT getInfoVct() {
        return infoVct;
    }

    public void setInfoVct(InfoNovedadVCT infoVct) {
        this.infoVct = infoVct;
    }

    public String getUsuarioOperacion() {
        return usuarioOperacion;
    }

    public void setUsuarioOperacion(String usuarioOperacion) {
        this.usuarioOperacion = usuarioOperacion;
    }
    
    public String getCsvTiposCotizantes() {
		return csvTiposCotizantes;
	}

	public void setCsvTiposCotizantes(String csvTiposCotizantes) {
		this.csvTiposCotizantes = csvTiposCotizantes;
	}
	
	public Afiliado copiarAfiliadoLlave() {
		
		Cobertura cobertura = new Cobertura();
		cobertura.setPoliza(this.cobertura.getPoliza());
		cobertura.setPeriodo(this.cobertura.getPeriodo());
		cobertura.setPeriodoGeneracion(this.cobertura.getPeriodoGeneracion());
		
		Condicion condicion = new Condicion();
		condicion.setTipoTasa(this.condicion.getTipoTasa());
		condicion.setPeriodoCotizacion(this.condicion.getPeriodoCotizacion());
		condicion.setIndicadorDias(this.condicion.getIndicadorDias());
		condicion.setTipoNovedad(this.condicion.getTipoNovedad());
		
		Afiliado afiliado = new Afiliado();
		afiliado.setCertificado(this.certificado);
		afiliado.setDni(this.dni);
		afiliado.setTipoAfiliado(this.tipoAfiliado);
		afiliado.setTipoCotizante(this.tipoCotizante);
		afiliado.setCobertura(cobertura);
		afiliado.setCondicion(condicion);
		
		return afiliado;
	}

	@Override
    public String toString() {
        return "Afiliado [dni=" + dni + ", tipoAfiliado=" + tipoAfiliado + ", tipoAfiliadoEstadoCuenta="
                + tipoAfiliadoEstadoCuenta + ", tipoCotizante=" + tipoCotizante + ", subtipoCotizante="
                + subtipoCotizante + ", salario=" + salario + ", tipoError=" + tipoError + ", ultimoIbc=" + ultimoIbc
                + ", certificado=" + certificado + ", nmroCoberturas=" + nmroCoberturas + ", fechaLimitePago="
                + fechaLimitePago + ", periodoCotizacion=" + periodoCotizacion + ", fechaInicioNovedad="
                + fechaInicioNovedad + ", fechaFinNovedad=" + fechaFinNovedad + ", tienePago=" + tienePago
                + ", dniEmpleador=" + dniEmpleador + ", esIndependiente=" + esIndependiente + ", esEstudiante="
                + esEstudiante + ", tieneNovedadIngreso=" + tieneNovedadIngreso + ", tieneNovedadRetiro="
                + tieneNovedadRetiro + ", tipoDocumentoEmpleador=" + tipoDocumentoEmpleador + ", cobertura=" + cobertura
                + ", condicion=" + condicion + ", legalizacion=" + legalizacion + ", infoVct=" + infoVct + "]";
    }

}
