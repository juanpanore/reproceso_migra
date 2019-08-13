package com.sura.arl.reproceso.actores.integrador;

import static com.sura.arl.reproceso.util.JSON.jsonToObjeto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.sura.arl.integrador.constantes.EstadoIntegrador;
import com.sura.arl.integrador.modelo.Registro;
import com.sura.arl.integrador.servicios.CambiosEstadoCuentaServicio;
import com.sura.arl.reproceso.actores.integrador.IntegradorEsperadaPullActor.RespuestaOperacionIntegrador;
import com.sura.arl.reproceso.actores.integrador.IntegradorEsperadaPullActor.SolicitudMensajes;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class CoordinadorIntegradorEsperada extends AbstractActor {

    private static final Integer NRO_REGISTROS_CAMBIOS = 100;
    protected final LoggingAdapter LOG = Logging.getLogger(context().system(),
            this);
    private int trabajoPendiente = 0;
    private CambiosEstadoCuentaServicio servicio;
    private VariablesEntorno env;
    List<Registro> registros;
    private AnnotationConfigApplicationContext contexto;

    public CoordinadorIntegradorEsperada(
            AnnotationConfigApplicationContext context) {
        LOG.info("Se creo coordinador");
        this.contexto = context;
        this.servicio = context.getBean(CambiosEstadoCuentaServicio.class);
        this.env = context.getBean(VariablesEntorno.class);
        this.registros = new ArrayList<Registro>();
        consultarRegistrosAProcesar();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SolicitudMensajes.class, this::enviarMensajes)
                .match(RespuestaOperacionIntegrador.class,
                        this::recibirRespuestaOperacion)
                // .match(Apagar.class, this::apagarSistema)
                .build();
    }

    private void apagarSistema() {

        LOG.info("Ingresamos a apagar sistema");

        try {
            TimeUnit.SECONDS.sleep(30);
            // TODO: Apagar sistema de actores, y aplicación
            contexto.close();
            System.exit(0);
        } catch (Exception e) {
            // Si sucede algun error no controlado se imprime traza y se cancela
            // el proceso.
            LOG.error(e.getMessage(), e);
            System.exit(0);
        }

    }

    private void recibirRespuestaOperacion(RespuestaOperacionIntegrador msg) {

        trabajoPendiente -= 1;
        LOG.info("Se recibe respuesta de operacion");

        if (msg.exitosa) {
            LOG.info(
                    "Se ha ejecutado la operacion {} exitosamente con mensaje {}. Trabajo pendiente {}",
                    msg.operacion, msg.respuesta, trabajoPendiente);
        } else {
            LOG.info(
                    "Ha ocurrido un error ejecutando la operacion {} con mensaje {}. Trabajo pendiente {}",
                    msg.operacion, msg.respuesta, trabajoPendiente);
        }

        if (trabajoPendiente == 0) {
            consultarRegistrosAProcesar();
        }
    }

    private void enviarMensajes(SolicitudMensajes msg) {

        LOG.info("Se recibe solicitud de msg, hay {} registros ",
                registros.size());

        if (registros.isEmpty()) {
            LOG.info(
                    "Se recibe solicitud, pero no hay registros, se buscan, y se reencola el mensaje");
            consultarRegistrosAProcesar();
            self().tell(msg, self());
        }

        int msgSolicitados = msg.cantidad;
        List<Registro> msgAEnviar = new ArrayList<Registro>();

        if (registros.size() >= msgSolicitados) {

            msgAEnviar = registros.subList(0, msgSolicitados);

        } else {
            msgAEnviar = registros.subList(0, registros.size());
        }

        msgAEnviar.stream().forEach(registro -> {

            registro.setMensajeMQ(servicio.generarMensajeMQ(registro));

            IntegradorEsperada integradorEsperada = null;
            try {
                integradorEsperada = jsonToObjeto(registro.getMensajeMQ(),
                        IntegradorEsperada.class);
            
            EstadoIntegrador estado = EstadoIntegrador.ENVIADO;
            registro.setEstado(estado.name());
            servicio.actualizarRegistroATramitar(registro);

            msg.trabajador.tell(integradorEsperada, self());
            trabajoPendiente += 1;
            } catch (Exception ex) {
                LOG.error("Error jsonToObjeto:" + registro.getMensajeMQ(), ex);
            }
        });

        registros.removeAll(msgAEnviar);
        // LOG.info("Quedaron {} registros, luego de enviar {} ",
        // registros.size(), msgAEnviar.size());
    }

    private void consultarRegistrosAProcesar() {

        // if (liderServicio.esLider()) {

        Integer limite = Integer.getInteger("negocio.numero.registros",
                NRO_REGISTROS_CAMBIOS);

        LOG.info("Limite de procesamiento {} ", limite);

        // Se marcan los registros a tramitar
        servicio.escogerRegistrosAGestionar();

        // Se consulta los registros marcados
        registros = servicio.consultarCambiosEstadoCuenta();
        LOG.info("Recursos recuperados {} ", registros.size());

        if (registros.isEmpty()) {

            LOG.info(
                    "LA CONSULTA NO RETORNO MAS RESULTADOS, TRABAJO PENDIENTE {}",
                    trabajoPendiente);

            if (trabajoPendiente == 0) {
                apagarSistema();
            }
        }
    }

}
