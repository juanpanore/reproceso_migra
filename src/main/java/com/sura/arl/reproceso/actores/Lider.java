package com.sura.arl.reproceso.actores;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sura.arl.reproceso.servicios.LiderServicio;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.LeaderChanged;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Este actor se emplea para verificar que nodo es el lider.
 *
 */
public class Lider extends AbstractActor {

	LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	Cluster cluster = Cluster.get(getContext().getSystem());

	boolean soyLider = false;

	private LiderServicio liderServicio;

	public Lider(ApplicationContext context) {
		liderServicio = context.getBean(LiderServicio.class);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(LeaderChanged.class, leader -> {
			String leaderAddress = leader.getLeader().hostPort();
			String iAddress = cluster.selfAddress().hostPort();
			log.info("Leader direccion {}", leaderAddress);
			log.info("Datos direccion {}", iAddress);

			if (iAddress.equals(leaderAddress)) {
				soyLider = true;
			} else {
				soyLider = false;
			}
			liderServicio.actualizaEstadoLider(soyLider);

		}).build();
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, LeaderChanged.class);
	}

	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

}
