package com.sura.arl.reproceso.modelo;

import java.util.Date;

public class PilaLlave {

	public final String pila;
	public final String operador;
	public final Date fechaPago;
	public final String tipoAjuste;

	private PilaLlave(String pila, String operador, Date fechaPago, String tipoAjuste) {
		super();
		this.pila = pila;
		this.operador = operador;
		this.fechaPago = fechaPago;
		this.tipoAjuste = tipoAjuste;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String pila;
		private String operador;
		private String tipoAjuste;
		private Date fechaPago;

		public Builder pila(String valor) {
			this.pila = valor;
			return this;
		}

		public Builder operador(String valor) {
			this.operador = valor;
			return this;
		}

		public Builder tipoAjuste(String valor) {
			this.tipoAjuste = valor;
			return this;
		}

		public Builder fechaPago(Date valor) {
			this.fechaPago = valor;
			return this;
		}

		public PilaLlave build() {
			return new PilaLlave(pila, operador, fechaPago, tipoAjuste);
		}
	}

	@Override
	public String toString() {
		return "PilaLlave [pila=" + pila + ", operador=" + operador + ", fechaPago=" + fechaPago + ", tipoAjuste="
				+ tipoAjuste + "]";
	}

}
