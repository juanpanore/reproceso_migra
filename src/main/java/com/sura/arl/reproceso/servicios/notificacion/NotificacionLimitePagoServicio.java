package com.sura.arl.reproceso.servicios.notificacion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.ErrorProceso.EstadoError;
import com.sura.arl.estadocuenta.servicios.ErroresProcesoServicio;
import com.sura.arl.reproceso.accesodatos.CoberturaDao;
import com.sura.arl.reproceso.accesodatos.NotificacionLimitePagoDao;
import com.sura.arl.reproceso.accesodatos.ParametrosDao;
import com.sura.arl.reproceso.actores.notificacion.CalcularFechaLimitePago.SolicitudConsultaPolizasNotificacion;
import com.sura.arl.reproceso.modelo.TipoGeneracion;
import com.sura.arl.reproceso.modelo.notificacion.EstadoNotificacionLimitePago;
import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.servicios.LiderServicio;
import com.sura.arl.reproceso.util.JSON;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.stream.ActorAttributes;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.Supervision;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Timeout;
import scala.compat.java8.FutureConverters;

@Service
public class NotificacionLimitePagoServicio {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionLimitePagoServicio.class);
    private static final int UNO = 1;
    private static final String NMRO_DIAS_HABIL_NOTIFICACION = "notificacion.numero.dias.habiles";
    private static final String NMRO_DIAS_HABIL_NOTIFICACION_DEFECTO = "3";
    private static final String NMRO_DIAS_HABIL_NO_AFILIADOS = "notificacion.numero.dias.habiles.noafiliados";
    private static final String NMRO_DIAS_HABIL_NO_AFILIADOS_DEFECTO = "16";
    private static final String DESC_ARCHIVO_NOTIFICACION_NO_GENERADO = "NO SE GENERO EL ARCHIVO DE NOTIFICACION";
    private static final String COBERTURA_TIENE_AFILIACON = "S";
    private static final String COBERTURA_NO_TIENE_AFILIACON = "N";
    private static Integer TAMANIO_PAQUETE = 1000;

    private final NotificacionLimitePagoDao dao;
    private final CoberturaDao coberturaDao;
    private final ParametrosDao parametrosDao;
    private final ErroresProcesoServicio errorProcesoServicio;
    private final ActorSelection calcularFechaLimitePagoActor;
    private final VariablesEntorno entorno;
    private final Materializer materializer;
    private final NotificacionLimitePagoIntegracionesServicio servicio;
    private Integer numeroDiasHabiles;
    private final LiderServicio liderServicio;

    @Autowired
    public NotificacionLimitePagoServicio(NotificacionLimitePagoDao dao, CoberturaDao coberturaDao,
            ParametrosDao parametrosDao, VariablesEntorno varEntorno, ErroresProcesoServicio errorProcesoServicio,
            NotificacionLimitePagoIntegracionesServicio servicio, ActorSystem sistemaActores,
            LiderServicio liderServicio) {
        this.dao = dao;
        this.coberturaDao = coberturaDao;
        this.parametrosDao = parametrosDao;
        this.calcularFechaLimitePagoActor = sistemaActores.actorSelection("/user/calcularFechaLimitePagoRouter");
        this.materializer = ActorMaterializer.create(sistemaActores);
        this.entorno = varEntorno;
        this.servicio = servicio;
        this.errorProcesoServicio = errorProcesoServicio;
        this.liderServicio = liderServicio;
    }

    //@Scheduled(cron = "0 30 5 * * *", zone = "GMT-5:00")
    private void iniciarProceso() {

        LOG.info("Iniciamos proceso notificacion limite pago");

        LOG.info("Es Lider servicio {} ", liderServicio.esLider());

        if (liderServicio.esLider()) {
            consultarParametros();
            procesarPeriodosVencidos();
            procesarPeriodosAnticipados();
        }
    }

    private void procesarPeriodosVencidos() {

        LOG.info("Procesando periodos vencidos");
        String periodoPago = obtenerPeriodoPago(TipoGeneracion.VENCIDA);
        LOG.info("periodo proceso vencido  obtenido {} ", periodoPago);

        LOG.info("Consultado la lista de polizas empresas y voluntarios");
        List<Cobertura> coberturas = coberturaDao.consultarCoberturasNotificacionFechaLimitePago(periodoPago);
        LOG.info("La cantidad de coberturas totales es {} ", coberturas.size());

        Map<String, List<Cobertura>> groupingByAfiliado = coberturas.stream()
                .collect(Collectors.groupingBy(Cobertura::getTieneAfiliacion));

        LOG.info("Coberturas {} ", groupingByAfiliado.keySet());

        procesarPeriodosVencidosAfiliados(periodoPago, groupingByAfiliado.get(COBERTURA_TIENE_AFILIACON));
        procesarPeriodosVencidosNoAfiliados(periodoPago, groupingByAfiliado.get(COBERTURA_NO_TIENE_AFILIACON));
    }

    private void procesarPeriodosVencidosAfiliados(String periodoPago, List<Cobertura> coberturas) {

        LOG.info("Procesando periodos vencidos afiliados");
        if (coberturas == null) {
            LOG.info("Termino la consulta de afiliados, SIN DATOS!");
            coberturas = Collections.emptyList();
        }
        LOG.info("La cantidad de coberturas es {} ", coberturas.size());

        Timeout askTimeout = Timeout.apply(60, TimeUnit.MINUTES);
        Source.from(coberturas).grouped(TAMANIO_PAQUETE).mapAsync(20, polizas -> {
            SolicitudConsultaPolizasNotificacion solicitud = new SolicitudConsultaPolizasNotificacion(periodoPago,
                    polizas, TipoGeneracion.VENCIDA);
            return FutureConverters
                    .toJava(Patterns.ask(calcularFechaLimitePagoActor, JSON.objetoToJson(solicitud), askTimeout));
        }).withAttributes(ActorAttributes.withSupervisionStrategy(e -> Supervision.resume()))
                .runWith(Sink.ignore(), materializer).whenComplete((done, e) -> {
                    if (e != null) {
                        LOG.error(e.getMessage(), e);
                    } else {
                        LOG.info("Termino la consulta de afiliados");
                        procesarNotificaciones(TipoGeneracion.VENCIDA, periodoPago);
                    }
                });

    }

    private void procesarPeriodosVencidosNoAfiliados(String periodoPago, List<Cobertura> coberturas) {

        LOG.info("Procesando periodos vencidos de no afiliados");
        if (coberturas == null) {
            LOG.info("Termino la consulta de no afiliados, SIN DATOS!");
            return;
        }

        Integer size = 100;
        Integer diasHabiles = consultarDiasHabilesNoAfiliados();
        LOG.info("Dias habiles {} de no afiliados", diasHabiles);
        Date fechaLimitePago = obtenerFechaLimitePago(periodoPago, diasHabiles);
        LOG.info("Fecha limite pago {} de no afiliados", fechaLimitePago);
        final AtomicInteger counter = new AtomicInteger(0);

        LOG.info("La cantidad de coberturas es {} de no afiliados", coberturas.size());
        coberturas.stream().map(cobertura -> {
            NotificacionLimitePago registro = new NotificacionLimitePago();
            registro.setPoliza(cobertura.getPoliza());
            registro.setEstadoNotificacion(EstadoNotificacionLimitePago.NO_AFILIADO);
            registro.setPeriodo(periodoPago);
            registro.setFechaLimitePago(fechaLimitePago);
            return registro;
        }).collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size)).forEach((key, registros) -> {
            try {
                dao.ingresar(registros);
                // LOG.info("Consulta de no afiliados {}
                // {}",key,registros.size());
            } catch (Exception ignore) {
                LOG.error(ignore.getMessage(), ignore);
            }
        });

        LOG.info("Termino la consulta de no afiliados");

    }

    private void procesarPeriodosAnticipados() {

        LOG.info("procesar periodo anticipado");
    }

    private void procesarNotificaciones(TipoGeneracion tipoGeneracion, String periodoPago) {

        LocalDate fechaLimitePagoNotificar = LocalDate.now();
        LocalDate fechaNotificacion = LocalDate.now();

        boolean esFechaPagoValida = esFechaLimitePagoValida(fechaLimitePagoNotificar);
        if (!esFechaPagoValida) {
            LOG.info("No es posible ejecutar el proceso los dias sabados, domingos o festivos");
            return;
        }

        // Obtenemos las notificaciones a realizar que coincidan con al fecha de
        // pago
        for (int i = 0; i < numeroDiasHabiles; i++) {
            // Obtenemos las notificaciones a realizar que coincidan con al
            // fecha de pago
            fechaLimitePagoNotificar = fechaLimitePagoNotificar.plusDays(UNO);
            fechaLimitePagoNotificar = obtenerFechaHabil(fechaLimitePagoNotificar, UNO);
        }

        // fechaLimitePagoNotificar = LocalDate.now();
        // fechaLimitePagoNotificar = LocalDate.of(2018, 03, 23);
        String nombreArchivo = "ARL_CP_FechaPago_"
                .concat(fechaNotificacion.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).concat(".csv");

        LOG.info("fecha limite pago notificar {} y periodo {} ", fechaLimitePagoNotificar, periodoPago);

        if (TipoGeneracion.VENCIDA.equals(tipoGeneracion)) {
            List<NotificacionLimitePago> notificaciones = dao.obtenerContratosPeriodoVencidoANotificarPorFecha(
                    convertirDesdeFechaLocal(fechaLimitePagoNotificar), periodoPago);
            LOG.info("numero de notificaciones ---> " + notificaciones.size());

            if (notificaciones.size() == 0) {
                return;
            }

            Map<Boolean, List<NotificacionLimitePago>> grupoNotificacionTieneErrores = notificaciones.stream()
                    .collect(Collectors.partitioningBy(NotificacionLimitePago::tieneErrores));

            Date fechaNotificacionActualizacion = new Date();

            // Tarea que genera el archivo de notificacion para los registros
            // sin error
            CompletableFuture.supplyAsync(() -> {

                List<NotificacionLimitePago> notificacionesExitosas = grupoNotificacionTieneErrores.get(false).stream()
                        .map(notificacion -> {
                            notificacion.setEstadoNotificacion(EstadoNotificacionLimitePago.NOTIFICADO);
                            notificacion.setNombreArchivo(nombreArchivo);
                            notificacion.setFechaNotificacion(fechaNotificacionActualizacion);
                            return notificacion;
                        }).collect(Collectors.toList());

                Optional<ByteArrayOutputStream> archivo = servicio.generarCsvNotificacion(notificacionesExitosas);

                if (archivo.isPresent()) {
                    try {
                        guardarArchivoNotificacionFtp(nombreArchivo,
                                new ByteArrayInputStream(archivo.get().toByteArray()));
                        dao.actualizar(grupoNotificacionTieneErrores.get(false));
                    } catch (IOException e) {
                        LOG.error("Error al subir archivos al ftp", e);
                        errorProcesoServicio.registrar(
                                obtenerErrorProceso(null, periodoPago, CatalogoErrores.ARCHIVO_NOTIFICACION_NO_GENERADO,
                                        DESC_ARCHIVO_NOTIFICACION_NO_GENERADO, tipoGeneracion));
                    }
                }
                return Collections.emptyList();
            });

            // Tarea que registra los errores de las notificaciones
            CompletableFuture.supplyAsync(() -> {

                List<NotificacionLimitePago> notificacionesErroneas = grupoNotificacionTieneErrores.get(true).stream()
                        .map(notificacion -> {
                            notificacion.setEstadoNotificacion(EstadoNotificacionLimitePago.FALLIDO);
                            notificacion.setFechaNotificacion(fechaNotificacionActualizacion);
                            return notificacion;
                        }).collect(Collectors.toList());

                List<ErrorProceso> erroresProceso = notificacionesErroneas.stream().flatMap(registro -> {
                    List<ErrorProceso> erroresNotificacion = new ArrayList<>();

                    registro.getObservaciones().forEach(observacion -> {
                        erroresNotificacion.add(obtenerErrorProceso(registro.getPoliza(), registro.getPeriodo(),
                                observacion.codigo, observacion.descripcion, tipoGeneracion));
                    });

                    return erroresNotificacion.stream();
                }).collect(Collectors.toList());

                dao.actualizar(notificacionesErroneas);
                errorProcesoServicio.registrar(erroresProceso);
                return Collections.emptyList();
            });
        }
    }

    private String obtenerPeriodoPago(TipoGeneracion tipoGeneracion) {

        LocalDate fechaCorte = LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
        for (int i = 0; i < numeroDiasHabiles - 1; i++) {
            fechaCorte = fechaCorte.minusDays(UNO);
            fechaCorte = obtenerFechaHabilHaciaAtras(fechaCorte, UNO);
        }

        LOG.info("Fecha de corte {} ", fechaCorte);

        LocalDate fechaPeriodo = LocalDate.now();

        if (fechaPeriodo.isBefore(fechaCorte) && TipoGeneracion.VENCIDA.equals(tipoGeneracion)) {
            fechaPeriodo = fechaPeriodo.plusMonths(-UNO);
        } else if (fechaPeriodo.isAfter(fechaCorte) && TipoGeneracion.VENCIDA.equals(tipoGeneracion)) {
            fechaPeriodo = fechaCorte;
        } else if (fechaPeriodo.isAfter(fechaCorte) && TipoGeneracion.ANTICIPADA.equals(tipoGeneracion)) {
            fechaPeriodo = fechaPeriodo.plusMonths(UNO);
        }

        return fechaPeriodo.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private boolean esFechaLimitePagoValida(LocalDate fecha) {

        return !dao.existeFechaFestiva(convertirDesdeFechaLocal(fecha));
    }

    private LocalDate obtenerFechaHabil(LocalDate fecha, long incrementoDias) {

        boolean resultado = esFechaLimitePagoValida(fecha);
        while (!resultado) {
            fecha = fecha.plusDays(incrementoDias);
            resultado = esFechaLimitePagoValida(fecha);
        }

        return fecha;
    }

    private LocalDate obtenerFechaHabilHaciaAtras(LocalDate fecha, long disminucionDias) {
        boolean resultado = esFechaLimitePagoValida(fecha);
        while (!resultado) {
            fecha = fecha.minusDays(disminucionDias);
            resultado = esFechaLimitePagoValida(fecha);
        }
        return fecha;
    }

    private void consultarParametros() {

        String parametroDiasHabiles = parametrosDao.obtenerTodosParametros().get(NMRO_DIAS_HABIL_NOTIFICACION);

        numeroDiasHabiles = Integer.parseInt(
                Objects.isNull(parametroDiasHabiles) ? NMRO_DIAS_HABIL_NOTIFICACION_DEFECTO : parametroDiasHabiles);

        LOG.info("Numero dias habiles parametrizados {} ", numeroDiasHabiles);
    }

    private Date convertirDesdeFechaLocal(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    private void guardarArchivoNotificacionFtp(String nombreArchivo, InputStream archivo)
            throws SocketException, IOException {

        String servidor = entorno.getValor("ftp.host");
        int puerto = Integer.valueOf(entorno.getValor("ftp.port"));
        String usuario = entorno.getValor("ftp.user");
        String contrasenia = entorno.getValor("ftp.password");
        String rutaDirectorio = entorno.getValor("ftp.rutaarchivos");

        servicio.guardarArchivoNotificacionFtp(nombreArchivo, rutaDirectorio, archivo, servidor, puerto, usuario,
                contrasenia);
        LOG.info("Archivo de notificacion {} subido correctamente a FTP", nombreArchivo);
    }

    private ErrorProceso obtenerErrorProceso(String poliza, String periodo, String codError, String observacion,
            TipoGeneracion tipoGeneracion) {

        ErrorProceso errorProceso = ErrorProceso.builder().codError(codError)
                .codigoProceso(entorno.getValor(VariablesEntorno.ID_PROCESO_NOTIFICACION))
                .usuarioRegistro(entorno.getValor(VariablesEntorno.DNI_INGRESA))
                .tipoGeneracion(tipoGeneracion.getEquivalencia())
                .periodoGeneracion(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")))
                .fechaRegistro(Calendar.getInstance().getTime()).npoliza(poliza).periodo(periodo)
                .observacion(observacion).estadoError(EstadoError.POR_CORREGIR).build();

        return errorProceso;
    }

    private Integer consultarDiasHabilesNoAfiliados() {

        String strParametroDiasHabiles = parametrosDao.obtenerTodosParametros().get(NMRO_DIAS_HABIL_NO_AFILIADOS);
        Integer diasHabiles = Integer
                .parseInt(Objects.isNull(strParametroDiasHabiles) ? NMRO_DIAS_HABIL_NO_AFILIADOS_DEFECTO
                        : strParametroDiasHabiles);

        LOG.info("Numero dias habiles parametrizados no afiliados {} ", diasHabiles);
        return diasHabiles;
    }

    private Date obtenerFechaLimitePago(String periodo, Integer diasHabiles) {

        return coberturaDao.obtenerDiaHabil(periodo, diasHabiles);

    }
}
