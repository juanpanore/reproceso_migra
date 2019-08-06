package com.sura.arl.reproceso.actores;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sura.arl.reproceso.modelo.ReprocesoCarga;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Este actor se encarga de ejecutar el reproceso.
 *
 */
@Component("ReprocesoCargaActor")
@Scope("prototype")
public class ReprocesoCargaActor extends AbstractActor {

    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private ReprocesoCargaServicio reprocesoCargaServicio;

    public ReprocesoCargaActor(ApplicationContext context) {
        super();
        reprocesoCargaServicio = context.getBean(ReprocesoCargaServicio.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReprocesoCarga.class, this::reprocesarCarga).build();
    }

    private void reprocesarCarga(ReprocesoCarga reprocesoCarga) {

        // Aplica correcciones en las esperadas segun la legalizacion.
        reprocesoCargaServicio.ejecutar(reprocesoCarga.getNumeroFormulario());

        sender().tell("OK", self());
    }

}
