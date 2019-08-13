package com.sura.arl.reproceso;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.sura.arl.estadocuenta.actores.CotizacionEsperadaActor;
import com.sura.arl.estadocuenta.actores.EstadoCuentaActor;
import com.sura.arl.integrador.servicios.GestionDeCambiosServicio;
import com.sura.arl.reproceso.actores.integrador.CoordinadorIntegradorEsperada;
import com.sura.arl.reproceso.actores.integrador.IntegradorEsperadaActor;
import com.sura.arl.reproceso.actores.integrador.IntegradorEsperadaPullActor;
import com.sura.arl.reproceso.config.AplicacionConfig;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada.TipoMensaje;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioActividad;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioDocumento;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioTasa;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCentroTrabajo;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCobertura;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaNuevoContrato;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaReversaLegalizacion;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import akka.stream.Materializer;

public class Aplicacion {

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
    
    private static final Logger LOG = LoggerFactory.getLogger(Aplicacion.class);
    private static GestionDeCambiosServicio gestionDeCambiosServicio;


    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AplicacionConfig.class);
        context.registerShutdownHook();

        context.getEnvironment().addActiveProfile("estadoCuenta");

        ActorSystem actorSystem = context.getBean(ActorSystem.class);
        Materializer materializer = context.getBean(Materializer.class);
        
        gestionDeCambiosServicio = context.getBean(GestionDeCambiosServicio.class);

        /*actorSystem.actorOf(Props.create(Lider.class, context), "lider");

        ActorRef reproceso = actorSystem.actorOf(
                FromConfig.getInstance().props(Props.create(ReprocesoCargaActor.class, context)), "reprocesoCarga");

        /*actorSystem.actorOf(FromConfig.getInstance().props(Props.create(CalcularFechaLimitePago.class, context)),
                "calcularFechaLimitePago");*/

        //actorSystem.actorOf(FromConfig.getInstance().props(), "calcularFechaLimitePagoRouter");

        ActorRef cotizacionActor = actorSystem.actorOf(
                FromConfig.getInstance().props(Props.create(CotizacionEsperadaActor.class, context)),
                "cotizacionActor");
        
        ActorRef coordinadorIntegradorActor = actorSystem.actorOf(Props.create(CoordinadorIntegradorEsperada.class, context));
        

        /*actorSystem.actorOf(FromConfig.getInstance().props(Props.create(ReprocesoAfiliadoActor.class, context)),
                "reprocesoAfiliadoActor");*/
        // Se cargan consumidores
        // Materializer materializer = ActorMaterializer.create(actorSystem);
        /*PlanillaLegalizadaConsumidor.activarConsumidor(context.getBean(AmqpConnectionDetails.class), reproceso,
                context.getBean(VariablesEntorno.class).getValor("broker.queue.planillas.legalizadas"), context);
*/
        //if (context.getEnvironment().acceptsProfiles("estadoCuenta")) {

            ActorRef estadoCuentaActorRef = actorSystem.actorOf(
                    FromConfig.getInstance().props(Props.create(EstadoCuentaActor.class, context)),
                    "estadoCuentaActor");

            // Activa consumidor para solicitudes de calculo de estado de cuenta
            /*EstadoCuentaConsumidor.activarConsumidor(context.getBean(AmqpConnectionDetails.class), estadoCuentaActorRef,
                    materializer,
                    context.getBean(VariablesEntorno.class).getValor("broker.queue.estadocuenta.solicitud"));
*/
            ActorRef integradorConsumidorActorRef = actorSystem.actorOf(
                    FromConfig.getInstance().props(Props.create(IntegradorEsperadaActor.class, context)),
                    "integradorEsperadaActor");
            
            
            ActorRef integradorConsumidorPullActorRef = actorSystem.actorOf(
                    FromConfig.getInstance().props(Props.create(IntegradorEsperadaPullActor.class, coordinadorIntegradorActor, context)),
                    "integradorEsperadaPullActor");
/*
            IntegradorEsperadaConsumidor.activarConsumidor(context.getBean(AmqpConnectionDetails.class),
                    integradorConsumidorActorRef, materializer,
                    context.getBean(VariablesEntorno.class).getValor("broker.queue.integrador.estado.cuenta"));

            actorSystem.actorOf(Props.create(ReprocesoCompletadoProductor.class, context), "reprocesoCompletadoProductor");*/
       // }

        // Solo cuando el nodo se haya unido al cluster, se inician otros
        // actores.
        /*Cluster.get(actorSystem).registerOnMemberUp(new Runnable() {
            @Override
            public void run() {
                LOG.info("MEMBER IS UP ");
                MemberClusterUp.memberClusterUp = true;
                actorSystem.eventStream().publish(MemberClusterUp.build());
            }
        });*/
        
        /*try {
            ejecutarIntegrador();
            TimeUnit.SECONDS.sleep(30);
            context.close();
            System.exit(0);
        } catch (Exception e) {
            // Si sucede algun error no controlado se imprime traza y se cancela
            // el proceso.
            LOG.error(e.getMessage(), e);
            System.exit(0);
        }*/

    }

    public static class MemberClusterUp {

        public volatile static boolean memberClusterUp = false;

        public static MemberClusterUp build() {
            return new MemberClusterUp();
        }

    }
    
    private static void ejecutarIntegrador() throws ParseException {
        System.out.println("---> buscando mensajes integrador...");
        gestionDeCambiosServicio.procesarCambiosEstadoCuentaMasivo();
    }

}
