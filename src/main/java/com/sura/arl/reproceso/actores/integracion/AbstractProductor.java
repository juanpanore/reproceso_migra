package com.sura.arl.reproceso.actores.integracion;

import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.actor.AbstractActor;
import akka.stream.Materializer;
import akka.stream.alpakka.amqp.AmqpConnectionDetails;
import akka.stream.alpakka.amqp.AmqpSinkSettings;
import akka.stream.alpakka.amqp.javadsl.AmqpSink;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.sura.arl.reproceso.Aplicacion.MemberClusterUp;

public abstract class AbstractProductor extends AbstractActor implements Productor {

    protected Materializer materializer;
    protected AmqpConnectionDetails connectionDetails;
    protected Sink<ByteString, CompletionStage<Done>> amqpSink;
    protected String exchange;
    protected String routingKey;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(MemberClusterUp.class, m -> activar()).match(String.class, this::enviar).build();
    }

    @Override
    public void enviar(String mensaje) {
        Source.single(mensaje).map(ByteString::fromString).runWith(amqpSink, materializer);
    }

    @Override
    public void activar() {
        amqpSink = AmqpSink.createSimple(
                AmqpSinkSettings.create(connectionDetails).withExchange(exchange).withRoutingKey(routingKey));
    }

    @Override
    public void preStart() throws Exception {
        context().system().eventStream().subscribe(self(), MemberClusterUp.class);
        if (MemberClusterUp.memberClusterUp) {
            this.activar();
        }
    }

}
