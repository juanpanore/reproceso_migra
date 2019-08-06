package com.sura.arl.estadocuenta.servicios;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.estadocuenta.accesodatos.ControlEstadoCuentaDao;
import com.sura.arl.estadocuenta.modelo.Afiliado.TipoAfiliado;
import com.sura.arl.estadocuenta.modelo.Afiliado.TipoCotizante;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.ControlEstadoCuenta;
import com.sura.arl.estadocuenta.modelo.ControlEstadoCuenta.TipoAfiliadoControl;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.reproceso.modelo.TipoGeneracion;
import com.sura.arl.reproceso.util.VariablesEntorno;

@Service
public class ControlEstadoCuentaServicio {

	private ControlEstadoCuentaDao dao;
	private ErroresProcesoServicio erroresProcesoServicio;
	private VariablesEntorno entorno;
	private static final Logger LOG = LoggerFactory.getLogger(ControlEstadoCuentaServicio.class);

	@Autowired
	public ControlEstadoCuentaServicio(ControlEstadoCuentaDao dao, ErroresProcesoServicio erroresProcesosServicio,
			VariablesEntorno entorno) {
		this.dao = dao;
		this.erroresProcesoServicio = erroresProcesosServicio;
		this.entorno = entorno;
	}

	public ControlEstadoCuenta consultar(String periodoCotizacion, String poliza,
			TipoAfiliadoControl tipoAfiliadoControl) {
		return dao.consultar(periodoCotizacion, poliza, tipoAfiliadoControl.getEquivalencia());
	}

	public Optional<Date> consultaFechaLimitePago(String poliza, String periodoCotizacion) {

		return dao.consultaFechaLimitePago(poliza, periodoCotizacion);
	}

	public Optional<String> consultarPeriodoCotizacion(String periodoGeneracion, String tipoAfiliado) {
		return dao.consultarPeriodoCotizacion(periodoGeneracion, tipoAfiliado);
	}

	@Transactional
	public void registrar(ControlEstadoCuenta control) {

		dao.registrar(control);
	}

	public void crearControlEstadoCuenta(String poliza, String periodoCotizacion, String periodoGeneracion,
			TipoAfiliadoControl tipoAfiliadoControl, int nmroAfiliadosEsperados, Date fechaLimitePago) {
		try {
			// Buscamos si existe control
			ControlEstadoCuenta control = consultar(periodoCotizacion, poliza, tipoAfiliadoControl);

			if (Objects.isNull(control)) {

				control = ControlEstadoCuenta.builder().periodoGeneracion(periodoGeneracion)
						.periodoCotizacion(periodoCotizacion).poliza(poliza)
						.totalAfiliadosInicial(nmroAfiliadosEsperados)
						.tipoAfiliadoControl(tipoAfiliadoControl.getEquivalencia()).fechaLimitePago(fechaLimitePago)
						.build();
				registrar(control);
			} else {
				if (control.getTotalAfiliadosInicial() == control.getTotalTrabajadoresInicial()) {
					LOG.debug(
							"No se procesa cobertura {} porque ya fue procesada, periodoCotizacion: {}, periodo generacion: {}, tipo afiliado control {}",
							poliza, periodoCotizacion, periodoGeneracion, tipoAfiliadoControl.getEquivalencia());
				}
			}
		} catch (Exception e) {
			LOG.error("Ha ocurrido un error al crear control estado cuenta ", e);
		}
	}

	public String obtenerPeriodoCotizacionPorTipoAfiliado(String periodoGeneracion, String tipoAfiliado,
			Supplier<String> valorDefecto) {

		String periodoCotizacion = valorDefecto.get();
		Optional<String> optPeriodoCotizacion = consultarPeriodoCotizacion(periodoGeneracion, tipoAfiliado);

		if (!optPeriodoCotizacion.isPresent()) {
			LOG.info(
					"No se encontró informacion de tipo generacion en las condiciones para tipo afiliado {}. Se toma el periodo vencido ",
					tipoAfiliado);

		} else {
			periodoCotizacion = optPeriodoCotizacion.get();
		}

		return periodoCotizacion;
	}

	@Transactional
	public void actualizarConsolidadoExcluyendoTipoCotizante(String poliza, String periodoCotizacion,
			TipoAfiliado tipoAfiliado, TipoCotizante tipoCotizanteExcluir, TipoAfiliadoControl tipoAfiliadoControl) {

		dao.actualizarConsolidadoExcluyendoTipoCotizante(poliza, periodoCotizacion, tipoAfiliado.getEquivalencia(),
				tipoCotizanteExcluir.getEquivalencia(), tipoAfiliadoControl.getEquivalencia());
	}

	@Transactional
	public void actualizarConsolidadoTipoCotizanteOpcional(String poliza, String periodoCotizacion,
			TipoAfiliado tipoAfiliado, Optional<TipoCotizante> tipoCotizanteOpcional,
			TipoAfiliadoControl tipoAfiliadoControl) {

		String tipoCotizante = tipoCotizanteOpcional.map(tc -> tc.getEquivalencia()).orElse(null);

		dao.actualizarConsolidadoTipoCotizanteOpcional(poliza, periodoCotizacion, tipoAfiliado.getEquivalencia(),
				tipoCotizante, tipoAfiliadoControl.getEquivalencia());
	}

	public Date consultarFechaLimitePago(String poliza, String periodoCotizacion, String periodoGeneracion) {

		Optional<Date> fechaLimitePago = consultaFechaLimitePago(poliza, periodoCotizacion);

		if (!fechaLimitePago.isPresent()) {

			ErrorProceso errorProceso = ErroresProcesoServicio.construir(poliza, periodoGeneracion, periodoCotizacion,
					CatalogoErrores.FECHA_LIMITE_PAGO_NO_CALCULADA,
					entorno.getValor("descError.fechaLimitePago.noCalculada"), TipoGeneracion.VENCIDA,
					entorno.getValor("id.proceso.notificacion"), entorno.getValor("usuario.dniingresa"));

			erroresProcesoServicio.registrarErrorProcesoUnico(errorProceso);
		}

		return fechaLimitePago.orElse(null);
	}
}
