package com.sura.arl.reproceso.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class RedondeosUtil {

	private static final Integer N100 = 100;
	private static final Integer N1 = 1;
	private static final Integer N1000 = 1000;
	private static final String PERIODO_REGLA = "201702";

	private RedondeosUtil() {
		super();
	}

	/**
	 * Redondea ibc segun periodo Si decreto 1707, se redondea al 1000 + cercano
	 * Si decreto 1990, se redondea al peso + cercano
	 * 
	 * 2018-12-04.Se cambia ceil por round para cumplir con la historia 1074
	 * redondear al 100 mas cercano
	 * 
	 * @param ibcActual
	 * @param periodo
	 * @return
	 */
	public static Double redondearIbc(Double ibcActual, String periodo) {
		if (!seAplicaRedondeoDecreto1990(periodo)) {
			return (double) Math.round((ibcActual) / N1000) * N1000;
		} else {
			return (double) Math.ceil(ibcActual);
		}
	}

	/**
	 * Redondea cotizacion segun periodo Si decreto 1707, se redondea al 100 +
	 * cercano Si decreto 1990, se redondea al 100 superior + cercano
	 * 
	 * @param cotizacion
	 * @param periodo
	 * @return
	 */
	public static Double redondearCotizacion(Double cotizacion, String periodo) {
            
            Double resultado ;
		if (!seAplicaRedondeoDecreto1990(periodo)) {
			resultado = (double) Math.round(cotizacion / N100) * N100;
		} else {
			resultado = (double) Math.ceil(cotizacion / N100) * N100;
		}
                
               return resultado; 
	}

	/**
	 * Si el periodo enviado es mayor de feb/2017, devuelve false por ser
	 * decreto 1707, de lo contrario, aplica decreto 1990
	 * 
	 * @param periodo
	 * @return
	 */
	private static boolean seAplicaRedondeoDecreto1990(String periodo) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyymm");

		try {
			if (!formatter.parse(periodo).before(formatter.parse(PERIODO_REGLA))) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
}
