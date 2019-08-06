package com.sura.arl.reproceso.actores.msg;

import com.sura.arl.afiliados.modelo.Afiliado;

public class ReprocesoAfiliadosMsg {
   
	private final long nmconsecutivo;
	private final Afiliado afiliado;

	private ReprocesoAfiliadosMsg(long nmconsecutivo, Afiliado afiliado) {
		this.nmconsecutivo = nmconsecutivo;
		this.afiliado = afiliado;
	}

	public static ReprocesoAfiliadosMsg crear(long nmconsecutivo, Afiliado afiliado) {
		return new ReprocesoAfiliadosMsg(nmconsecutivo, afiliado);
	}

	public Afiliado getAfiliado() {
		return this.afiliado;
	}
	
	public long getNmconsecutivo() {
		return nmconsecutivo;
	}
}
