package com.sura.arl.reproceso.actores.integrador;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioActividad;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioDocumento;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioTasa;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCentroTrabajo;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCobertura;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaReversaLegalizacion;
import com.sura.arl.reproceso.servicios.integrador.IntegradorEsperadaAfiliadoServicio;

import org.springframework.context.ApplicationContext;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaPullActor extends AbstractActor {

    protected final LoggingAdapter LOG = Logging.getLogger(context().system(), this);
    private final IntegradorEsperadaAfiliadoServicio afiliadoServicio;
    private final ActorRef coordinador;
    private int peticiones = 0;
    
    public IntegradorEsperadaPullActor(ActorRef coordinador, ApplicationContext context) {
        super();
        LOG.info("Entramos a crear actor ");

        this.coordinador = coordinador;
        this.afiliadoServicio = context.getBean(IntegradorEsperadaAfiliadoServicio.class);
        solicitarMensajes();
    }
    
    private void solicitarMensajes() {
        
        if(peticiones < 2) {
            
            coordinador.tell(new SolicitudMensajes(self(), 5), self());
            this.peticiones += 5;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(IntegradorEsperadaCobertura.class, this::procesarIntegradorEsperada)
                .match(IntegradorEsperadaCentroTrabajo.class, this::procesarIntegradorEsperada)
                .match(IntegradorEsperadaAfiliado.class, this::procesarIntegradorEsperada)
                .match(IntegradorEsperadaCambioTasa.class, this::procesarIntegradorEsperada)
                .match(IntegradorEsperadaCambioDocumento.class, this::procesarIntegradorEsperada)
                .match(IntegradorEsperadaCambioActividad.class, this::procesarIntegradorEsperada)
                .match(IntegradorEsperadaReversaLegalizacion.class, this::procesarIntegradorEsperada)
                .build();
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaAfiliado integradorEsperada) {
        
        //LOG.info("Procesar Integrador Esperada:" + integradorEsperada.toString());
        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            switch (integradorEsperada.getTipo()) {
                case AFILIACION:
                    afiliadoServicio.registrarAfiliado(integradorEsperada);
                    break;
                case RETIRO:
                    afiliadoServicio.retirarAfiliado(integradorEsperada);
                    break;
                case ANULACION:
                    afiliadoServicio.borrarAfiliado(integradorEsperada);
                    break;
                case ACTUALIZACIONNOVEDAD:
                    afiliadoServicio.novedadAfiliado(integradorEsperada);
                    break;
                case REPROCESO_FLUJO_COMPLETO:
                    afiliadoServicio.reprocesoFlujoCompleto(integradorEsperada);
                    break;
                default:
                    LOG.debug("Validar Tipo de Mensaje - procesarIntegradorEsperada");
            }
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador(String.format("consecutivo %s", integradorEsperada.getId()), "IntegradorEsperadaAfiliado", true), getSelf());
        } catch (Exception e) {
            LOG.error(e, "Error poliza {}, dni {}, message: {}", integradorEsperada.getPoliza(), integradorEsperada.getDniAfiliado(), e.getLocalizedMessage());
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "procesarIntegradorEsperada", false), getSelf());

        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCobertura integradorEsperada) {
        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            //LOG.info("Procesar Integrador Cobertura:" + integradorEsperada.toString());
            afiliadoServicio.movimientoCoberturaAfiliado(integradorEsperada);
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador("OK", "IntegradorEsperadaCobertura", true), getSelf());

        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "IntegradorEsperadaCobertura", false), getSelf());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCambioTasa integradorEsperada) {

        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            //LOG.info("Procesar Integrador Cobertura:" + integradorEsperada.toString());
            afiliadoServicio.cambiarTasa(integradorEsperada);
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador(String.format("consecutivo %s", integradorEsperada.getId()), "IntegradorEsperadaCambioTasa", true), getSelf());

        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "IntegradorEsperadaCambioTasa", false), getSelf());

        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCentroTrabajo integradorEsperada) {
        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            //LOG.info("Procesar Integrador Cobertura:" + integradorEsperada.toString());
            afiliadoServicio.cambiarCentroTrabajo(integradorEsperada);
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador(String.format("consecutivo %s", integradorEsperada.getId()), "IntegradorEsperadaCentroTrabajo", true), getSelf());

        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            e.printStackTrace();
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "IntegradorEsperadaCentroTrabajo", false), getSelf());

        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCambioDocumento integradorEsperada) {

        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            //LOG.info("Procesar Cambio Documento:" + integradorEsperada.toString());
            afiliadoServicio.cambioDocumento(integradorEsperada);
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador(String.format("consecutivo %s", integradorEsperada.getId()), "IntegradorEsperadaCambioDocumento", true), getSelf());

        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getDniAfiliado(), e.getLocalizedMessage());
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "IntegradorEsperadaCambioDocumento", false), getSelf());

        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCambioActividad integradorEsperada) {

        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            //LOG.info("Procesar Cambio actividad independiente:" + integradorEsperada.toString());
            afiliadoServicio.cambiarActividadIndependiente(integradorEsperada);
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador(String.format("consecutivo %s", integradorEsperada.getId()), "IntegradorEsperadaCambioActividad", true), getSelf());

        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "IntegradorEsperadaCambioActividad", false), getSelf());

        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaReversaLegalizacion integradorEsperada) {

        try {
            this.peticiones -=1;
            solicitarMensajes();
            
            //LOG.info("Procesar Reversa legalizacion:" + integradorEsperada.toString());
            afiliadoServicio.reversarLegalizacion(integradorEsperada);
            //getSender().tell("OK", self());
            coordinador.tell(new RespuestaOperacionIntegrador("OK", "IntegradorEsperadaReversaLegalizacion", true), getSelf());

        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            //sender().tell("ERROR", self());
            coordinador.tell(new RespuestaOperacionIntegrador(e.getMessage(), "IntegradorEsperadaReversaLegalizacion", false), getSelf());

        }
    }
    
    final class RespuestaOperacionIntegrador {
        
        public final String respuesta;
        public final boolean exitosa;
        public final String operacion;
        
        public RespuestaOperacionIntegrador(String respuesta, String operacion, boolean exitosa) {
            this.respuesta = respuesta;
            this.operacion = operacion;
            this.exitosa = exitosa;
        }
        
    }
    
    final class SolicitudMensajes {
        
        public final int cantidad;
        public final ActorRef trabajador;
        
        public SolicitudMensajes(ActorRef trabajador, int cantidad) {
            this.trabajador = trabajador;
            this.cantidad = cantidad;
        }
    }
}
