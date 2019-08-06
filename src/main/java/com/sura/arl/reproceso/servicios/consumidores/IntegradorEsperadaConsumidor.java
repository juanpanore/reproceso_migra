package com.sura.arl.reproceso.servicios.consumidores;

import akka.NotUsed;
import akka.actor.ActorRef;
import static akka.pattern.Patterns.ask;
import static com.sura.arl.reproceso.util.JSON.jsonToObjeto;

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

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioActividad;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada.TipoMensaje;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioDocumento;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioTasa;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCancelacionContrato;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCentroTrabajo;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCobertura;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaNuevoContrato;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaReversaLegalizacion;

import static scala.compat.java8.FutureConverters.toJava;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 *
 * @author pragma.co
 */
public final class IntegradorEsperadaConsumidor {

    static {
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.AFILIACION, IntegradorEsperadaAfiliado.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.ANULACION, IntegradorEsperadaAfiliado.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.RETIRO, IntegradorEsperadaAfiliado.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.ACTUALIZACIONNOVEDAD, IntegradorEsperadaAfiliado.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.MOVER_COBERTURA, IntegradorEsperadaCobertura.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.CAMBIO_TASA_CT, IntegradorEsperadaCambioTasa.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.CAMBIO_CENTRO_TRABAJO, IntegradorEsperadaCentroTrabajo.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.CAMBIO_DOCUMENTO, IntegradorEsperadaCambioDocumento.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.CANCELACION_CONTRATO, IntegradorEsperadaCobertura.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.AFILIACION_IND, IntegradorEsperadaNuevoContrato.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.CAMBIO_ACTIVIDAD_IND, IntegradorEsperadaCambioActividad.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.REVERSA_LEGALIZACION, IntegradorEsperadaReversaLegalizacion.class);
        IntegradorEsperada.addIntegradorEsperadaRegistro(TipoMensaje.REPROCESO_FLUJO_COMPLETO, IntegradorEsperadaAfiliado.class);
    }

    private static final Logger LOG = LoggerFactory.getLogger(IntegradorEsperadaConsumidor.class);
    private static final Integer BUFFER_SIZE = 200;

    public static void activarConsumidor(AmqpConnectionDetails connectionProvider, ActorRef actor,
            Materializer materializer, String nombreCola) {
/*
        // Variables configuracion flujo consumidor
        Timeout askTimeout = Timeout.apply(60, TimeUnit.SECONDS);
        FiniteDuration duracionMinimaReintento = Duration.apply(3L, TimeUnit.SECONDS);
        FiniteDuration duracionMaximaReintento = Duration.apply(60L, TimeUnit.SECONDS);
        Double factorRetardoEntreReintentos = 0.2;
        Integer paralelizacion = 10;

        // Conecta flujo al broker.
        // Configura flujo para confirmar o devolver mensajes consumidos.
        Source<CommittableIncomingMessage, NotUsed> amqpSource = AmqpSource
                .committableSource(NamedQueueSourceSettings.create(connectionProvider, nombreCola), BUFFER_SIZE);

        // Configura flujo para reintentar fallos de conexion con el broker.
        Source<CommittableIncomingMessage, NotUsed> mainStream = RestartSource.withBackoff(duracionMinimaReintento,
                duracionMaximaReintento, factorRetardoEntreReintentos, () -> amqpSource);

        // Se transforma mensaje procesandolo con el actor programado.
        mainStream.mapAsync(paralelizacion, cm -> {

            String rawMessage = cm.message().bytes().utf8String();
            IntegradorEsperada integradorEsperada = null;
            try {
                integradorEsperada = jsonToObjeto(rawMessage, IntegradorEsperada.class);
            } catch (Exception ex) {
                LOG.error("Error jsonToObjeto:" + rawMessage, ex);
            }
            return toJava(ask(actor, integradorEsperada, askTimeout)).whenComplete((obj, e) -> {

                if (obj instanceof String) {
                    LOG.error("ACTOR MENSAJE {} ", obj);
                    if (((String) obj).toLowerCase().contains("error")) {
                        cm.nack(true, false);
                    } else {
                        cm.ack(false);
                    }
                } else if (e != null) {
                    LOG.error("SE DESCARTA MENSAJE " + rawMessage, e);
                    cm.nack(false, false);
                } else {
                    cm.ack(false);
                }
            });

        }).withAttributes(ActorAttributes.withSupervisionStrategy(e -> Supervision.resume())).runWith(Sink.ignore(),
                materializer);
 */           
    }

}
