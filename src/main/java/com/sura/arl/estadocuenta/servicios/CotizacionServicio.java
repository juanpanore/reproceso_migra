package com.sura.arl.estadocuenta.servicios;

import org.springframework.stereotype.Service;

import com.sura.arl.reproceso.util.RedondeosUtil;

@Service
public class CotizacionServicio {
    static final Integer N100 = 100;

	public Double calcularCotizacion(Double tasa, Double ibc, String periodo) {

        Double cotizacion = ibc * tasa / N100;

		// redondea la cotizacion
		return RedondeosUtil.redondearCotizacion(cotizacion, periodo);
	}

}
