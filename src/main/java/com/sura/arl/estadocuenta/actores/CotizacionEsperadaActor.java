package com.sura.arl.estadocuenta.actores;

import org.springframework.context.ApplicationContext;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class CotizacionEsperadaActor extends AbstractActor {

    protected final LoggingAdapter LOG = Logging.getLogger(context().system(), this);

    EstadoCuentaServicio esperadaServicio;

    public CotizacionEsperadaActor(ApplicationContext context) {
        super();
        esperadaServicio = context.getBean(EstadoCuentaServicio.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Afiliado.class, this::procesarAfiliado).build();
    }

    /**
     *
     * Es imperativo que el actor emita una respuesta al emisor del mensaje
     * recibido por este actor.
     *
     * @param afiliado
     */
    private void procesarAfiliado(Afiliado afiliado) {
        EstadoCuenta esperada = esperadaServicio.calcularEstadoCuenta(afiliado);
        if (esperada != null) {
            LOG.info("Calculo de Estado de Cuenta OK");
            getSender().tell(esperada, self());
        } else {
            LOG.info("Calculo de Estado de Cuenta ERROR");
            sender().tell("ERROR", self());
        }
    }

}
