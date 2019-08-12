package com.sura.arl.reproceso.servicios;

import java.net.NoRouteToHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.base.Stopwatch;
import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.ControlEstadoCuentaDao;
import com.sura.arl.reproceso.accesodatos.ControlNovedadesDao;
import com.sura.arl.reproceso.accesodatos.IngresoRetiroAfiliacionDao;
import com.sura.arl.reproceso.accesodatos.NotificacionLimitePagoDao;
import com.sura.arl.reproceso.accesodatos.NovedadesDao;
import com.sura.arl.reproceso.accesodatos.ReprocesoPendienteDao;
import com.sura.arl.reproceso.actores.ReprocesoAfiliadoActor;
import com.sura.arl.reproceso.modelo.ReprocesoCompletado;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;
import com.sura.arl.reproceso.servicios.generales.InexactitudConsolidadoServicio;
import com.sura.arl.reproceso.util.JSON;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.routing.FromConfig;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Timeout;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

@Service
public class ReprocesoCargaServicio {

    private static final Logger LOG = LoggerFactory.getLogger(ReprocesoCargaServicio.class);
    private static final int MENSJ_X_ACTOR = 20;

    private final NovedadesDao novedadesDao;
    private final ControlNovedadesDao controlNovedadesDao;
    private final ControlEstadoCuentaDao controlEstadoCuentaDao;
    private final IngresoRetiroAfiliacionDao ingresoRetiroAfiliacionDao;
    private final InexactitudConsolidadoServicio inexactitudServicio;
    private final ReprocesoPendienteDao reprocesoPendienteDao;
    private final NotificacionLimitePagoDao notificacionLimitePagoDao;

    private ActorRef reprocesoAfiliadoActor;
    private ActorSelection productor;

    @Autowired
    private VariablesEntorno varEntorno;

    @Autowired
    Materializer materializer;

    @Autowired
    ApplicationContext context;

    ActorSystem actorSystem;

    @Autowired
    public ReprocesoCargaServicio(NovedadesDao novedadesDao, ControlNovedadesDao controlNovedadesDao,
            IngresoRetiroAfiliacionDao ingresoRetiroAfiliacionDao, ApplicationContext context, ActorSystem actorSystem,
            ControlEstadoCuentaDao controlEstadoCuentaDao, InexactitudConsolidadoServicio inexactitudServicio,
            ReprocesoPendienteDao reprocesoPendienteDao, NotificacionLimitePagoDao notificacionLimitePagoDao) {
        super();
        this.novedadesDao = novedadesDao;
        this.controlNovedadesDao = controlNovedadesDao;
        this.ingresoRetiroAfiliacionDao = ingresoRetiroAfiliacionDao;
        this.controlEstadoCuentaDao = controlEstadoCuentaDao;
        this.context = context;
        this.reprocesoAfiliadoActor = actorSystem.actorOf(
                FromConfig.getInstance().props(Props.create(ReprocesoAfiliadoActor.class, context)),
                "reprocesoAfiliadoActor");
        this.productor = actorSystem.actorSelection("/user/reprocesoCompletadoProductor");

        this.inexactitudServicio = inexactitudServicio;
        this.reprocesoPendienteDao = reprocesoPendienteDao;
        this.notificacionLimitePagoDao = notificacionLimitePagoDao;
        this.actorSystem = actorSystem;
    }

    /**
     * Ejecuta el reproceso para numero de formulario especifico
     * 
     * @param numFormulario
     * @throws InterruptedException
     */

    public void ejecutar(Long numFormulario ) {
        Objects.requireNonNull(numFormulario, "El numero de formulario no puede ser nulo");

        // Obtiene los afiliados con novedades de ingreso y/o retiro y los
        // ingresa para el proceso de afiliacion

        // TODO: se comenta este proceso hasta saber q se va hacer con el
        // ingresarNovedadIngresoRetiroAfiliaciones(numFormulario);

        Stopwatch timer = new Stopwatch().start();
        LOG.info("Buscando afiliados para el fomulario: {} ...", numFormulario);

        List<Afiliado> afiliados = novedadesDao.obtenerAfiliadosXformulario(numFormulario, Optional.empty(),
                Optional.empty());
        timer.stop();

        LOG.info("Afiliados encontrados:{} para formulario:{}, tiempo: {}", afiliados.size(), numFormulario,
                timer.elapsed(TimeUnit.SECONDS));

        // solo deberia empezar a procesar si encontro afiliados
        if (!afiliados.isEmpty()) {

            if (!esFormularioAnticipado(afiliados.get(0).getCobertura().getPoliza(),
                    afiliados.get(0).getCobertura().getPeriodoAnioMes(), numFormulario)) {

                CountDownLatch latch = new CountDownLatch(1);

                Source.from(afiliados)
                        .ask(MENSJ_X_ACTOR, reprocesoAfiliadoActor, String.class, Timeout.apply(5, TimeUnit.MINUTES))
                        .runWith(Sink.ignore(), materializer).whenComplete((done, e) -> {
                            if (e != null) {
                                LOG.error(
                                        "Error reprocesando el formulario {} con error {}, reenviando a reproceso el formulario.",
                                        numFormulario, e.getMessage(), e);
                                ejecutar(numFormulario);
                            } else {
                                LOG.info("Se ha reprocesado exitosamente el formulario {} ", numFormulario);
                                enviarMensajeConsolidador(afiliados.get(0), Optional.of(numFormulario));
                            }
                            latch.countDown();
                        });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    LOG.info("Ocurrio un error al esperar por el procesamiento del formulario {} ", numFormulario, e);
                }
            } else {
                LOG.info("No se reprocesa formulario:{}, por ser pago anticipado, se registra en reprocesos pendientes",
                        numFormulario);
            }
        } else {
            LOG.info("No se reprocesa formulario:{}, sin registros", numFormulario);
        }
    }
    
    /**
     * Ejecuta el reproceso para numero de formulario especifico
     * 
     * @param numFormulario
     * @throws InterruptedException
     */

    public void ejecutarReprocesoAfiliado(Long numFormulario, IntegradorEsperadaAfiliado afiliado) {
        Objects.requireNonNull(numFormulario, "El numero de formulario no puede ser nulo");

        // Obtiene los afiliados con novedades de ingreso y/o retiro y los
        // ingresa para el proceso de afiliacion

        // TODO: se comenta este proceso hasta saber q se va hacer con el
        // ingresarNovedadIngresoRetiroAfiliaciones(numFormulario);

        Stopwatch timer = new Stopwatch().start();
        LOG.info("Buscando afiliados para el fomulario: {} ...", numFormulario);

        List<Afiliado> afiliados = novedadesDao.obtenerAfiliadoXformulario(numFormulario, afiliado);
        timer.stop();

        LOG.info("Afiliados encontrados:{} para formulario:{}, tiempo: {}", afiliados.size(), numFormulario,
                timer.elapsed(TimeUnit.SECONDS));

        // solo deberia empezar a procesar si encontro afiliados
        if (!afiliados.isEmpty()) {

            if (!esFormularioAnticipado(afiliados.get(0).getCobertura().getPoliza(),
                    afiliados.get(0).getCobertura().getPeriodoAnioMes(), numFormulario)) {

                CountDownLatch latch = new CountDownLatch(1);

                Source.from(afiliados)
                        .ask(MENSJ_X_ACTOR, reprocesoAfiliadoActor, String.class, Timeout.apply(5, TimeUnit.MINUTES))
                        .runWith(Sink.ignore(), materializer).whenComplete((done, e) -> {
                            if (e != null) {
                                LOG.error(
                                        "Error reprocesando el formulario {} con error {}, reenviando a reproceso el formulario.",
                                        numFormulario, e.getMessage(), e);
                                ejecutar(numFormulario);
                            } else {
                                LOG.info("Se ha reprocesado exitosamente el formulario {} ", numFormulario);
                                enviarMensajeConsolidador(afiliados.get(0), Optional.of(numFormulario));
                            }
                            latch.countDown();
                        });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    LOG.info("Ocurrio un error al esperar por el procesamiento del formulario {} ", numFormulario, e);
                }
            } else {
                LOG.info("No se reprocesa formulario:{}, por ser pago anticipado, se registra en reprocesos pendientes",
                        numFormulario);
            }
        } else {
            LOG.info("No se reprocesa formulario:{}, sin registros", numFormulario);
        }
    }

    public void enviarMensajeConsolidador(Afiliado afiliado, Optional<Long> numeroFormulario) {
        enviarMensajeConsolidador(afiliado.getCobertura().getPoliza(), afiliado.getCobertura().getPeriodoAnioMes(),
                numeroFormulario);
    }

    public void enviarMensajeConsolidador(String poliza, String periodo, Optional<Long> numeroFormulario) {
        ReprocesoCompletado rc = new ReprocesoCompletado(poliza, null, null, periodo, numeroFormulario.orElse(null));

        Future<Object> futuro = Patterns.ask(productor, JSON.objetoToJson(rc), Timeout.apply(5, TimeUnit.SECONDS));

        // Se valida el envio del mensaje al RabittMQ
        FutureConverters.toJava(futuro).whenComplete((done, e) -> {
            if (e instanceof NoRouteToHostException) {
                LOG.error("Error al enviar mensaje al consolidador, poliza:{}, periodo:{}, formulario:{}",
                        poliza, poliza,
                        numeroFormulario.get());
            }
        });

    }

    private void ingresarNovedadIngresoRetiroAfiliaciones(Long numeroFormulario) {

        List<Afiliado> afiliados = novedadesDao.obtenerNovedadIngresoRetiroAfiliadosXformulario(numeroFormulario);
        LOG.info("Total afiliados con novedad ingreso o retiro:{} para forumulario:{}", afiliados.size(),
                numeroFormulario);

        for (Afiliado afiliado : afiliados) {
            // LOG.info("Afiliado con ingreso o retiro {}", afiliado.getDni());

            // Si el afiliado tiene novedad de ingreso o retiro se registra
            // para ejecutar la novedad
            // en los sistemas de afiliacion
            if (afiliado.tieneNovedadIngreso() || afiliado.tieneNovedadRetiro()) {
                ingresoRetiroAfiliacionDao.registrar(numeroFormulario, afiliado);
            }
        }
    }

    private boolean esFormularioAnticipado(String poliza, String periodo, Long numFormulario) {
        // Calendar calendar = Calendar.getInstance();
        Optional<Date> fechaLimite = notificacionLimitePagoDao.consultaFechaLimitePago(poliza, periodo);

        if (!fechaLimite.isPresent()) {
            LOG.info(
                    "No se encontro fechaLimitePago para poliza:{}, periodo:{}, se sigue continua reproceso de formiulario:{} ",
                    poliza, periodo, numFormulario);
            return false;
        }

        try {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMM");
            LocalDate fechaCotizacion = dateformat.parse(periodo).toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate fechaLimitePago = fechaLimite.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            // LocalDate periodoActual = LocalDate.now();

            if (fechaCotizacion.isAfter(fechaLimitePago)) {

                reprocesoPendienteDao.registrar(poliza, periodo, numFormulario);
                return true;
            }

        } catch (ParseException e) {
            LOG.error(
                    "Error parseando fechas en formularioAnticipado, periodoCotizacion:{}, fechaLimitePago:{}, error:{}",
                    periodo, fechaLimite.get().toString(), e.getMessage());
        }

        return false;

    }

    public VariablesEntorno getVarEntorno() {
        return varEntorno;
    }

    public void setVarEntorno(VariablesEntorno varEntorno) {
        this.varEntorno = varEntorno;
    }

}
