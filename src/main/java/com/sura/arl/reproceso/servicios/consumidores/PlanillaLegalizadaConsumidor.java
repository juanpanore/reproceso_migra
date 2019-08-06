package com.sura.arl.reproceso.servicios.consumidores;

import static com.sura.arl.reproceso.util.JSON.jsonToObjeto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.sura.arl.reproceso.modelo.PlanillaLegalizada;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
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
import scala.concurrent.duration.Duration;

public class PlanillaLegalizadaConsumidor {

    private static final Logger LOG = LoggerFactory.getLogger(PlanillaLegalizadaConsumidor.class);
    private static final Integer bufferSize = 50;
    private static final String DISPATCHER_REPROCESOFLUJO = "flujoReproceso-dispatcher";

    public static void activarConsumidor(AmqpConnectionDetails connectionProvider, ActorRef actor, String nombreCola,
            ApplicationContext contexto) {

        Materializer materializer = contexto.getBean(Materializer.class);
        ReprocesoCargaServicio reprocesoCargaServicio = contexto.getBean(ReprocesoCargaServicio.class);
        ActorSystem actorSystem = contexto.getBean(ActorSystem.class);

        // Timeout askTimeout = Timeout.apply(60, TimeUnit.SECONDS);

        Source<CommittableIncomingMessage, NotUsed> amqpSource = AmqpSource
                .committableSource(NamedQueueSourceSettings.create(connectionProvider, nombreCola), bufferSize);

        Source<CommittableIncomingMessage, NotUsed> mainStream = RestartSource.withBackoff(
                Duration.apply(3, TimeUnit.SECONDS), Duration.apply(60, TimeUnit.SECONDS), 0.2, () -> amqpSource);
        
        
        mainStream.mapAsyncUnordered(bufferSize, cm -> {
        	
            String rawMessage = cm.message().bytes().utf8String();
            
            try {
                PlanillaLegalizada planillaLegalizada = jsonToObjeto(rawMessage, PlanillaLegalizada.class);
               	
                return CompletableFuture
                        .runAsync(() -> reprocesoCargaServicio.ejecutar(planillaLegalizada.getNumeroFormulario()),
                                actorSystem.dispatchers().lookup(DISPATCHER_REPROCESOFLUJO))
                        .whenComplete((respuesta, e) -> {
                            if (e != null) {
                                LOG.error("SE DESCARTA MENSAJE {} con  error {}, clase {}", rawMessage,e.getMessage(), e.getCause());
                                
                                if(e.getCause() instanceof CannotGetJdbcConnectionException) {
                                	LOG.info("NO SE OBTUVO UNA CONEXION DISPONIBLE. SE REENCOLA EL MENSAJE");
                                	cm.nack(false, true);
                                }
                                cm.nack(false, false);
                            } else {
                                cm.ack(false);
                            }
                        });
            } catch (IllegalArgumentException ie) {
                LOG.error("Ocurrio un error al deserializar el mensaje {}", rawMessage, ie);
                cm.nack(false, false);
                return null;
            }

        }).withAttributes(ActorAttributes.withSupervisionStrategy(e -> Supervision.resume())).runWith(Sink.ignore(),
                materializer);

    }

}