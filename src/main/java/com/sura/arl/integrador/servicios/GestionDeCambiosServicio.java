package com.sura.arl.integrador.servicios;

import static com.sura.arl.reproceso.util.JSON.jsonToObjeto;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.sura.arl.integrador.constantes.EstadoIntegrador;
import com.sura.arl.integrador.modelo.Registro;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;
import com.sura.arl.reproceso.servicios.LiderServicio;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

@Service
public class GestionDeCambiosServicio {

    private static final Logger LOG = LoggerFactory.getLogger(GestionDeCambiosServicio.class);

    private CambiosEstadoCuentaServicio servicio;

    private VariablesEntorno env;

    private ActorSystem actorSystem;

    private Materializer materializer;

    private ActorSelection productor;

    private LiderServicio liderServicio;

    @Autowired
    public GestionDeCambiosServicio(ApplicationContext context) {
        super();
        this.servicio = context.getBean(CambiosEstadoCuentaServicio.class);
        this.env = context.getBean(VariablesEntorno.class);
        this.actorSystem = context.getBean(ActorSystem.class);
        this.materializer = ActorMaterializer.create(actorSystem);
        this.productor = context.getBean(ActorSystem.class).actorSelection("/user/integradorEsperadaActor");
        this.liderServicio = context.getBean(LiderServicio.class);
    }

//    @Scheduled(cron = "${job.cron.tramitar.cambios.estado.cuenta}", zone = "${job.zona}")
    // @Scheduled(cron = "*/60 * * * * ?", zone = "GMT-5")
    public void tramitarCambiosEstadoCuenta() {

        LOG.info("<<---- Inicio del Proceso de Cambios para el Estado de Cuenta ---->>");

        // if (liderServicio.esLider()) {
        Double inicio = 0D;
        Double tamano = 500D;
        Double fin = tamano; 
        
        while (true) {
            try {

                // Se marcan los registros a tramitar
                // servicio.escogerRegistrosAGestionar();
                LOG.info("obteniendo mensajes {} / {}",inicio, fin);
                // Se consulta los registros marcados
                List<Registro> registros = servicio.consultarCambiosEstadoCuenta(inicio, fin);
                LOG.info("mensajes encontrados: {}",registros.size());
                if(registros.size()<1) {
                    break;
                }
                
                // List<Registro> registros = new ArrayList<Registro>();
                CountDownLatch latch = new CountDownLatch(registros.size());

                // Se gestiona el mensaje a RabittMQ
                registros.parallelStream().forEach(registro -> {

                    // Se crea el mensaje para RabittMQ
                    registro.setMensajeMQ(servicio.generarMensajeMQ(registro));

                    IntegradorEsperada integradorEsperada = null;
                    try {
                        integradorEsperada = jsonToObjeto(registro.getMensajeMQ(), IntegradorEsperada.class);
                    } catch (Exception ex) {
                        LOG.error("Error jsonToObjeto:" + registro.getMensajeMQ(), ex);
                    }
                    EstadoIntegrador estado = EstadoIntegrador.ENVIADO;
                    registro.setEstado(estado.name());
                    servicio.actualizarRegistroATramitar(registro);

                    // Se realiza el envio del mensaje al RabittMQ
                    Future<Object> futuro = Patterns.ask(productor,
                            integradorEsperada, Timeout.apply(5, TimeUnit.MINUTES));
                    try {
                        Await.result(futuro,
                                Timeout.apply(5, TimeUnit.MINUTES).duration());
                    } catch (Exception e) {
                        LOG.error("Error al esperar resultado del integrador", e);
                    } finally {
                        latch.countDown();
                        LOG.info("Registros restantes {} ", latch.getCount());
                    }
                });
                latch.await();
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            
            inicio = tamano+1;
            fin = fin + tamano;
        }
        /*
         * } else { LOG.
         * info("<<---- Fin del Proceso de Cambios para el Estado de Cuenta - NO LIDER---->>"
         * ); }
         */

        LOG.info("<<---- Fin del Proceso de Cambios para el Estado de Cuenta ---->>");
    }

}
