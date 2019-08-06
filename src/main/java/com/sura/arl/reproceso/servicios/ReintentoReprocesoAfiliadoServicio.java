package com.sura.arl.reproceso.servicios;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sura.arl.reproceso.accesodatos.LogEjecucionReprocesoDao;
import com.sura.arl.reproceso.actores.msg.ReprocesoAfiliadosMsg;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.FromConfig;

@Service
public class ReintentoReprocesoAfiliadoServicio {

	private static final Logger LOG = LoggerFactory.getLogger(ReintentoReprocesoAfiliadoServicio.class);

	private final LogEjecucionReprocesoDao logEjecucionDao;

	private final ActorRef reprocesoAfiliadoRouter;

	private final LiderServicio liderServicio;

	@Autowired
	public ReintentoReprocesoAfiliadoServicio(LogEjecucionReprocesoDao logEjecucionDao, LiderServicio liderServicio,
			ActorSystem actorSystem) {

		this.logEjecucionDao = logEjecucionDao;
		this.reprocesoAfiliadoRouter = actorSystem.actorOf(FromConfig.getInstance().props(), "reprocesoAfiliadoRouter");
		this.liderServicio = liderServicio;

	}
	
	@Scheduled(fixedRate = 300000, initialDelay = 10000)
	private void iniciarProceso() {

		LOG.info("Se inicia proceso para reintentar reprocesar afiliados no reprocesados");

		LOG.info("Es Lider servicio {} ", liderServicio.esLider());

		if (liderServicio.esLider()) {

			List<ReprocesoAfiliadosMsg> listaAfiliados = logEjecucionDao.obtenerAfiliadosParaReprocesar();
			LOG.info("El numero de afiliados es {} ", listaAfiliados.size());
			listaAfiliados.stream().forEach(msg -> reprocesoAfiliadoRouter.tell(msg, ActorRef.noSender()));
		}
	}
}
