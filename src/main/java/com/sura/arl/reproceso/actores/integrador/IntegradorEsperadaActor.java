package com.sura.arl.reproceso.actores.integrador;

import akka.actor.AbstractActor;
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
public class IntegradorEsperadaActor extends AbstractActor {

    protected final LoggingAdapter LOG = Logging.getLogger(context().system(), this);

    private final IntegradorEsperadaAfiliadoServicio afiliadoServicio;

    public IntegradorEsperadaActor(ApplicationContext context) {
        super();
        this.afiliadoServicio = context.getBean(IntegradorEsperadaAfiliadoServicio.class);
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

        LOG.info("Procesar Integrador Esperada:" + integradorEsperada.toString());
        try {
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
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error(e, "Error poliza {}, dni {}, message: {}", integradorEsperada.getPoliza(), integradorEsperada.getDniAfiliado(), e.getLocalizedMessage());
            sender().tell("ERROR", self());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCobertura integradorEsperada) {
        try {
            LOG.info("Procesar Integrador Cobertura:" + integradorEsperada.toString());
            afiliadoServicio.movimientoCoberturaAfiliado(integradorEsperada);
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            sender().tell("ERROR", self());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCambioTasa integradorEsperada) {

        try {
            LOG.info("Procesar Integrador Cobertura:" + integradorEsperada.toString());
            afiliadoServicio.cambiarTasa(integradorEsperada);
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            sender().tell("ERROR", self());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCentroTrabajo integradorEsperada) {
        try {
            LOG.info("Procesar Integrador Cobertura:" + integradorEsperada.toString());
            afiliadoServicio.cambiarCentroTrabajo(integradorEsperada);
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            e.printStackTrace();
            sender().tell("ERROR", self());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCambioDocumento integradorEsperada) {

        try {
            LOG.info("Procesar Cambio Documento:" + integradorEsperada.toString());
            afiliadoServicio.cambioDocumento(integradorEsperada);
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getDniAfiliado(), e.getLocalizedMessage());
            sender().tell("ERROR", self());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaCambioActividad integradorEsperada) {

        try {
            LOG.info("Procesar Cambio actividad independiente:" + integradorEsperada.toString());
            afiliadoServicio.cambiarActividadIndependiente(integradorEsperada);
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            sender().tell("ERROR", self());
        }
    }

    private void procesarIntegradorEsperada(IntegradorEsperadaReversaLegalizacion integradorEsperada) {

        try {
            LOG.info("Procesar Reversa legalizacion:" + integradorEsperada.toString());
            afiliadoServicio.reversarLegalizacion(integradorEsperada);
            getSender().tell("OK", self());
        } catch (Exception e) {
            LOG.error("Error poliza {},  message: {}", integradorEsperada.getPoliza(), e.getLocalizedMessage());
            sender().tell("ERROR", self());
        }
    }
}
