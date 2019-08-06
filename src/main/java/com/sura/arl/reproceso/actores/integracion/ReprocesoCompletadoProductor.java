package com.sura.arl.reproceso.actores.integracion;

import org.springframework.context.ApplicationContext;

import akka.stream.Materializer;
import akka.stream.alpakka.amqp.AmqpConnectionDetails;
import com.sura.arl.reproceso.Aplicacion.MemberClusterUp;
import com.sura.arl.reproceso.util.VariablesEntorno;

/**
 *
 * @author pragma.co
 */
public class ReprocesoCompletadoProductor extends AbstractProductor {

    ApplicationContext applicationContext;

    public ReprocesoCompletadoProductor(ApplicationContext context) {

        this.applicationContext = context;
        this.connectionDetails = context.getBean(AmqpConnectionDetails.class);
        this.materializer = context.getBean(Materializer.class);

        VariablesEntorno entorno = context.getBean(VariablesEntorno.class);
        exchange = entorno.getValor("broker.exchange.reproceso.completado");
        routingKey = entorno.getValor("broker.routingkey.reproceso.completado");

    }

    @Override
    public Receive createReceive() {
        // Solo se conecta al broker si cumple con el perfil
        //if (applicationContext.getEnvironment().acceptsProfiles("estadoCuenta")) {
            return receiveBuilder()
                    .match(MemberClusterUp.class, m -> activar())
                    .match(String.class, this::enviar)
                    .build();
        /*} else {
            return receiveBuilder().build();
        }*/
    }

}
