package com.sura.arl.estadocuenta.modelo;

import java.util.Date;


public class ControlEstadoCuenta {

	private final String poliza;
	private final String periodoGeneracion;
	private final String periodoCotizacion;
	private final String tipoAfiliadoControl;
	private final int totalAfiliadosInicial;
	private final int totalTrabajadoresInicial;
	private final int totalTrabajadores;
	private final long expuestosInicial;
	private final double valorEsperadoInicial;
	private final double valorEsperado;
	private final double valorSaldoInicial;
	private final double deuda;
	private final double saldoFavor;
	private final Date fechaLimitePago;
	private final DecisionSimple conciliado;
	private final DecisionSimple reabierto;
	private final String usuarioOperacion;
	private final EstadoPagoControl estadoPago;

	
	private ControlEstadoCuenta(Builder builder){
		
		this.poliza = builder.poliza;
		this.periodoGeneracion = builder.periodoGeneracion;
		this.periodoCotizacion = builder.periodoCotizacion;
		this.tipoAfiliadoControl = builder.tipoAfiliadoControl;
		this.totalAfiliadosInicial = builder.totalAfiliadosInicial;
		this.totalTrabajadoresInicial = builder.totalTrabajadoresInicial;
		this.totalTrabajadores = builder.totalTrabajadores;
		this.expuestosInicial = builder.expuestosInicial;
		this.valorEsperadoInicial = builder.valorEsperadoInicial;
		this.valorEsperado = builder.valorEsperado;
		this.valorSaldoInicial = builder.valorSaldoInicial;
		this.deuda = builder.deuda;
		this.saldoFavor = builder.saldoFavor;
		this.fechaLimitePago = builder.fechaLimitePago;
		this.conciliado = builder.conciliado;
		this.reabierto = builder.reabierto;
		this.usuarioOperacion = builder.usuarioOperacion;
		this.estadoPago = builder.estadoPago;
	}
	
	public String getPoliza() {
		return poliza;
	}

	public String getPeriodoGeneracion() {
		return periodoGeneracion;
	}

	public String getPeriodoCotizacion() {
		return periodoCotizacion;
	}

	public int getTotalAfiliadosInicial() {
		return totalAfiliadosInicial;
	}

	public int getTotalTrabajadoresInicial() {
		return totalTrabajadoresInicial;
	}
	
	public int getTotalTrabajadores() {
		return totalTrabajadores;
	}
	
	public long getExpuestosInicial() {
		return expuestosInicial;
	}
	
	public double getValorEsperadoInicial(){
		return valorEsperadoInicial;
	}
	
	public double getValorEsperado() {
		return valorEsperado;
	}
	
	public double getValorSaldoInicial() {
		return valorSaldoInicial;
	}
	
	public double getDeuda() {
		return deuda;
	}
	
	public double getSaldoFavor() {
		return saldoFavor;
	}
	
	public Date getFechaLimitePago() {
		return fechaLimitePago;
	}
	
	public DecisionSimple getConciliado() {
		return conciliado;
	}
	
	public DecisionSimple getReabierto() {
		return reabierto;
	}

	public String getUsuarioOperacion() {
		return usuarioOperacion;
	}
	
	public String getTipoAfiliadoControl() {
		return tipoAfiliadoControl;
	}
	
	public EstadoPagoControl getEstadoPago() {
		return estadoPago;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String poliza;
		private String periodoGeneracion;
		private String periodoCotizacion;
		private int totalAfiliadosInicial;
		private int totalTrabajadoresInicial;
		private int totalTrabajadores;
		private Date fechaLimitePago;
		private String usuarioOperacion;
		private String tipoAfiliadoControl;
		private long expuestosInicial;
		private double valorEsperadoInicial;
		private double valorEsperado;
		private double valorSaldoInicial;
		private double deuda;
		private double saldoFavor;	
		private DecisionSimple conciliado = DecisionSimple.N;
		private DecisionSimple reabierto = DecisionSimple.N;
		private EstadoPagoControl estadoPago = EstadoPagoControl.MORA_PRESUNTA_TOTAL;
		
		public Builder poliza(String valor){
			this.poliza = valor;
			return this;
		}
		
		public Builder periodoGeneracion(String valor){
			this.periodoGeneracion = valor;
			return this;
		}
		
		public Builder periodoCotizacion(String valor){
			this.periodoCotizacion = valor;
			return this;
		}
		
		public Builder totalAfiliadosInicial(int valor){
			this.totalAfiliadosInicial = valor;
			return this;
		}
		
		public Builder totalTrabajadoresInicial(int valor){
			this.totalTrabajadoresInicial = valor;
			return this;
		}
		
		public Builder totalTrabajadores(int valor){
			this.totalTrabajadores = valor;
			return this;
		}
		
		public Builder expuestosInicial(long valor){
			this.expuestosInicial = valor;
			return this;
		}
		
		public Builder valorEsperadoInicial(double valor){
			this.valorEsperadoInicial = valor;
			return this;
		}
		
		public Builder valorEsperado(double valor){
			this.valorEsperado = valor;
			return this;
		}
		
		public Builder valorSaldoInicial(double valor){
			this.valorSaldoInicial = valor;
			return this;
		}
		
		public Builder deuda(double valor){
			this.deuda = valor;
			return this;
		}
		
		public Builder saldoFavor(double valor){
			this.saldoFavor = valor;
			return this;
		}
		
		public Builder conciliado(DecisionSimple valor){
			this.conciliado = valor;
			return this;
		}
		
		public Builder reabierto(DecisionSimple valor){
			this.reabierto = valor;
			return this;
		}
		
		public Builder fechaLimitePago(Date valor){
			this.fechaLimitePago = valor;
			return this;
		}
		
		public Builder usuarioOperacion(String valor){
			this.usuarioOperacion = valor;
			return this;
		}
		
		public Builder tipoAfiliadoControl(String valor){
			this.tipoAfiliadoControl = valor;
			return this;
		}
		
		public Builder estadoPago(EstadoPagoControl valor) {
			this.estadoPago = valor;
			return this;
		}
		
		public ControlEstadoCuenta build(){
			return new ControlEstadoCuenta(this);
		}
	}
	
	public enum TipoAfiliadoControl {

		DEPENDIENTE("01"), INDEPENDIENTE("02"), ESTUDIANTE("03");

		private String equivalencia;

		TipoAfiliadoControl(String equivalencia) {
			this.equivalencia = equivalencia;
		}

		public String getEquivalencia() {
			return this.equivalencia;
		}
	}
	
	public enum DecisionSimple {
	    S,N;
	}
	
	public enum EstadoPagoControl {
		
		MORA_PRESUNTA_TOTAL("01"), MORA_TOTAL("02"), MORA_PARCIAL("03"), INEXACTITUD_PAGOS("04");
		
		private String equivalencia;
		
		EstadoPagoControl(String equivalencia) {
			this.equivalencia = equivalencia;
		}

		public String getEquivalencia() {
			return this.equivalencia;
		}
	}
}