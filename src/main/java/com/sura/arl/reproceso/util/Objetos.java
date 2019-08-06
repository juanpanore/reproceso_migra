package com.sura.arl.reproceso.util;

import java.util.Collection;
import java.util.List;

public class Objetos {

	public static void requiereNoVacio(List<?> lista, String mensaje) {
		if (lista == null || lista.isEmpty()) {
			throw new IllegalArgumentException(mensaje);
		}
	}

	public static void requiereNoNulo(Object obj, String mensaje) {
		if (obj == null) {
			throw new IllegalArgumentException(mensaje);
		}
	}

	public static boolean esNulo(Object obj) {
		return obj == null;
	}

	public static boolean esVacio(Collection<?> obj) {
		return esNulo(obj) || obj.isEmpty();
	}

}
