package com.sura.arl.reproceso.servicios;

import org.springframework.stereotype.Service;

/**
 * Este servicio es stateful y se emplea para guardar si el nodo que lo ejecuta
 * es el lider del cluster.
 *
 */
@Service
public class LiderServicio {

	private boolean esLider = false;

	public void actualizaEstadoLider(boolean valor) {
		esLider = valor;
	}

	public boolean esLider() {
		return esLider;
	}

}
