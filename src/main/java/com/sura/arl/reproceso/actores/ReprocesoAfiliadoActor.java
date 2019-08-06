package com.sura.arl.reproceso.actores;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.reproceso.accesodatos.LogEjecucionReprocesoDao;
import com.sura.arl.reproceso.accesodatos.LogEjecucionReprocesoDao.TipoErrorEjecucion;
import com.sura.arl.reproceso.actores.msg.ReprocesoAfiliadosMsg;
import com.sura.arl.reproceso.modelo.ResultadoRecalculo;
import com.sura.arl.reproceso.servicios.generales.ReprocesoAfiliadoServicio;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ReprocesoAfiliadoActor extends AbstractActor {

	private static final String DISPATCHER_REPROCESO_PROCESAMIENTO = "IO-dispatcher";

	protected final LoggingAdapter log = Logging.getLogger(context().system(), this);

	private ReprocesoAfiliadoServicio reprocesoAfiliadoServicio;

	private LogEjecucionReprocesoDao logEjecucionReprocesoDao;

	public ReprocesoAfiliadoActor(ApplicationContext context) {
		super();
		this.reprocesoAfiliadoServicio = context.getBean(ReprocesoAfiliadoServicio.class);
		this.logEjecucionReprocesoDao = context.getBean(LogEjecucionReprocesoDao.class);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Afiliado.class, this::procesarAfiliado)
				.match(ReprocesoAfiliadosMsg.class, this::procesarAfiliado).build();
	}

	/**
	 * 
	 * Es imperativo que el actor emita una respuesta al emisor del mensaje
	 * recibido por este actor.
	 * 
	 * @param afiliado
	 */
	private void procesarAfiliado(Afiliado afiliado) {

		// log.info("Se recibe mensaje para procesar afiliado con dni {} ",
		// afiliado.getDni());

		try {
			ejecutarRecalculo(afiliado).get(5, TimeUnit.MINUTES);
			sender().tell("OK", self());
		} catch (Exception e) {
			log.error("Se almacena el afiliado con dni {} en el log al no poder reprocesar el tiempo configurado ",
					afiliado.getDni(), e);

			if (e instanceof TimeoutException) {
				String mensaje = Objects.isNull(e.getMessage()) ? ""
						: e.getMessage().length() > 200 ? e.getMessage().substring(0, 200) : e.getMessage();

				logEjecucionReprocesoDao.registrar(afiliado, TipoErrorEjecucion.TIMEOUT, mensaje);
				sender().tell("ERROR", self());
			}
		}
	}

	private CompletableFuture<ResultadoRecalculo> ejecutarRecalculo(Afiliado afiliado) {

		return CompletableFuture.supplyAsync(() -> {
			try {
				return reprocesoAfiliadoServicio.ejecutarRecalculo(afiliado);
			} catch (Exception e) {
				if (e.getCause() instanceof CannotGetJdbcConnectionException) {
					String mensaje = Objects.isNull(e.getMessage()) ? ""
							: e.getMessage().length() > 200 ? e.getMessage().substring(0, 200) : e.getMessage();
					logEjecucionReprocesoDao.registrar(afiliado, TipoErrorEjecucion.TIMEOUT, mensaje);
				}
				return new ResultadoRecalculo();
			}
		}, getContext().system().dispatchers().lookup(DISPATCHER_REPROCESO_PROCESAMIENTO));
	}

	private void procesarAfiliado(ReprocesoAfiliadosMsg msg) {

		logEjecucionReprocesoDao.actualizar(msg.getNmconsecutivo());
		procesarAfiliado(msg.getAfiliado());
	}

}
