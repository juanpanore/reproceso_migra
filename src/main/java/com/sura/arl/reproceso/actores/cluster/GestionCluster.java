package com.sura.arl.reproceso.actores.cluster;

import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Cancellable;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.LeaderChanged;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberJoined;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

public class GestionCluster extends AbstractActor {

	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private Cluster cluster = Cluster.get(getContext().getSystem());

	public GestionCluster(ApplicationContext context) {
		getContext().getSystem().scheduler().scheduleOnce(Duration.create(1, TimeUnit.MINUTES),
				this::completeMaintenance,
				getContext().getSystem().dispatchers().lookup("akka.actor.default-blocking-io-dispatcher"));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ClusterEvent.MemberUp.class, this::memberUp)
				.match(MemberRemoved.class, this::memberRemoved).match(MemberJoined.class, this::memberJoined)
				.match(UnreachableMember.class, this::memberUnreachable).match(LeaderChanged.class, this::leaderChanged)
				.build();
	}

	private void memberUp(MemberUp msj) {
		log.info("MEMBER_UP {}", msj.member().address().hostPort());
		log.info("SELF_ADDRESS {}", cluster.selfAddress());

		if (cluster.selfAddress().hostPort().equals(msj.member().address().hostPort())) {
		}

	}

	private void memberRemoved(MemberRemoved msj) {
		log.info("MEMBER_REMOVED {}", msj.member().address().hostPort());
	}

	private void leaderChanged(LeaderChanged msj) {
		log.info("!leaderChanged************************");
		log.info("Leader {}", cluster.state().leader().get());
		log.info("Members {}", cluster.state().getMembers());
		log.info("Member unreachable{}", cluster.state().getUnreachable());
		log.info("************************!");
	}

	private void memberJoined(MemberJoined msj) {
		log.info("MEMBER_JOINED {}", msj.member().address().hostPort());
	}

	private void memberUnreachable(UnreachableMember msj) {
		log.info("MEMBER_UNREACHABLE {}", msj.member().address().hostPort());
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, MemberUp.class,
				MemberRemoved.class, MemberJoined.class, UnreachableMember.class, LeaderChanged.class);
	}

	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
		tickTask.cancel();
	}

	/**
	 * El mantenimiento solo se ejecuta en el nodo lider.
	 */
	private void completeMaintenance() {
		String myAddress = cluster.selfAddress().hostPort();
		String leaderAddress = cluster.state().getLeader().hostPort();

		// Si es lider se ejecuta
		if (myAddress.equals(leaderAddress)) {
			log.info("Ejecutando mantenimiento");
			log.info("Active members {}", cluster.state().getMembers());
		}
	}

	ActorSelection cache = this.context().actorSelection("/user/cache");
	private static final String TICK = "tick";
	private final Cancellable tickTask = getContext().getSystem().scheduler()
			.schedule(Duration.create(30, TimeUnit.SECONDS), Duration.create(30, TimeUnit.MINUTES), () -> {

				String myAddress = cluster.selfAddress().hostPort();
				String leaderAddress = cluster.state().getLeader().hostPort();
				// Si es lider se ejecuta
				if (myAddress.equals(leaderAddress)) {
					cache.tell(TICK, ActorRef.noSender());
				}

			}, context().dispatcher());
}
