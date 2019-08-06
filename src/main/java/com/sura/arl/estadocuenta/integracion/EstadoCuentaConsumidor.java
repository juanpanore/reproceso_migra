package com.sura.arl.estadocuenta.integracion;

import static akka.pattern.Patterns.ask;
import static com.sura.arl.reproceso.util.JSON.jsonToObjeto;
import static scala.compat.java8.FutureConverters.toJava;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sura.arl.estadocuenta.modelo.SolicitudEstadoCuenta;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.stream.ActorAttributes;
import akka.stream.Materializer;
import akka.stream.Supervision;
import akka.stream.alpakka.amqp.AmqpConnectionDetails;
import akka.stream.alpakka.amqp.NamedQueueSourceSettings;
import akka.stream.alpakka.amqp.javadsl.AmqpSource;
import akka.stream.alpakka.amqp.javadsl.CommittableIncomingMessage;
import akka.stream.javadsl.RestartSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public final class EstadoCuentaConsumidor {

    private static final Logger LOG = LoggerFactory.getLogger(EstadoCuentaConsumidor.class);
    private static final Integer bufferSize = 200;

    public static void activarConsumidor(AmqpConnectionDetails connectionProvider, ActorRef actor,
            Materializer materializer, String nombreCola) {

        // Variables configuracion flujo consumidor
        Timeout askTimeout = Timeout.apply(60, TimeUnit.SECONDS);
        FiniteDuration duracionMinimaReintento = Duration.apply(3L, TimeUnit.SECONDS);
        FiniteDuration duracionMaximaReintento = Duration.apply(60L, TimeUnit.SECONDS);
        Double factorRetardoEntreReintentos = 0.2;
        Integer paralelizacion = 10;

        // Conecta flujo al broker.
        // Configura flujo para confirmar o devolver mensajes consumidos.
        Source<CommittableIncomingMessage, NotUsed> amqpSource = AmqpSource
                .committableSource(NamedQueueSourceSettings.create(connectionProvider, nombreCola), bufferSize);

        // Configura flujo para reintentar fallos de conexion con el broker.
        Source<CommittableIncomingMessage, NotUsed> mainStream = RestartSource.withBackoff(duracionMinimaReintento,
                duracionMaximaReintento, factorRetardoEntreReintentos, () -> amqpSource);

        // Se transforma mensaje procesandolo con el actor programado.
        mainStream.mapAsync(paralelizacion, cm -> {

            String rawMessage = cm.message().bytes().utf8String();

            SolicitudEstadoCuenta solicitudEstadoCuenta = jsonToObjeto(rawMessage, SolicitudEstadoCuenta.class);

            return toJava(ask(actor, solicitudEstadoCuenta, askTimeout)).whenComplete((obj, e) -> {
                if (e != null) {
                    LOG.error("SE DESCARTA MENSAJE " + rawMessage, e);
                    cm.nack(false, false);
                } else {
                    cm.ack(false);
                }
            });

        }).withAttributes(ActorAttributes.withSupervisionStrategy(e -> Supervision.resume())).runWith(Sink.ignore(),
                materializer);

    }

}
