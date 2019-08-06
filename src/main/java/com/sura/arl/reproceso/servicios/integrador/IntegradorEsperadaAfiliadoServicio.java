package com.sura.arl.reproceso.servicios.integrador;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.accesodatos.AfiliadosCoberturaDao;
import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Legalizacion;
import com.sura.arl.estadocuenta.modelo.Afiliado.TipoAfiliado;
import com.sura.arl.estadocuenta.modelo.Afiliado.TipoCotizante;
import com.sura.arl.estadocuenta.modelo.ControlEstadoCuenta.TipoAfiliadoControl;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.servicios.ControlEstadoCuentaServicio;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.integrador.accesodatos.CambiosEstadoCuentaDao;
import com.sura.arl.integrador.accesodatos.ControlIntegradorDao;
import com.sura.arl.integrador.modelo.CambioIntegrador;
import com.sura.arl.integrador.modelo.ControlIntegrador;
import com.sura.arl.integrador.modelo.EstadoIntegrador;
import com.sura.arl.reproceso.accesodatos.ControlNovedadesDao;
import com.sura.arl.reproceso.accesodatos.NotificacionLimitePagoDao;
import com.sura.arl.reproceso.accesodatos.NovedadesDao;
import com.sura.arl.reproceso.accesodatos.ParametrosDao;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioActividad;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioDocumento;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCambioTasa;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCentroTrabajo;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaCobertura;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaReversaLegalizacion;
import com.sura.arl.reproceso.servicios.RenesServicio;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;
import com.sura.arl.reproceso.servicios.generales.ReprocesoAfiliadoServicio;
import com.sura.arl.reproceso.util.Periodo;
import com.sura.arl.reproceso.util.UtilCadenas;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorAttributes;
import akka.stream.Materializer;
import akka.stream.Supervision;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

/**
 *
 * @author pragma.co
 */
@Service
public class IntegradorEsperadaAfiliadoServicio {

    private static final Logger LOG = LoggerFactory.getLogger(IntegradorEsperadaAfiliadoServicio.class);

    private static final int CANTIDAD_MENSAJES = 50;

    private final AfiliadosCoberturaDao afiliadosCoberturaDao;

    private final ControlNovedadesDao controlNovedadesDao;

    private final CambiosEstadoCuentaDao cambiosEstadoCuentaDao;

    private final ControlIntegradorDao controlIntegradorDao;

    private final NotificacionLimitePagoDao notificacionLimitePagoDao;

    private final NovedadesDao novedadesDao;

    private final ParametrosDao parametrosDao;

    private final EstadoCuentaServicio esperadaServicio;

    private final RenesServicio renesServicio;

    private final ReprocesoAfiliadoServicio reprocesoAfiliadoServicio;

    private final Materializer materializer;

    private final ActorSystem actorSystem;

    private final ReprocesoCargaServicio reprocesoCargaServicio;

    private final ControlEstadoCuentaServicio controlEstadoCuentaServicio;

    @Autowired
    public IntegradorEsperadaAfiliadoServicio(AfiliadosCoberturaDao afiliadosCoberturaDao,
            ControlNovedadesDao controlNovedadesDao, CambiosEstadoCuentaDao cambiosEstadoCuentaDao,
            ControlIntegradorDao controlIntegradorDao, NovedadesDao novedadesDao, ParametrosDao parametrosDao,
            NotificacionLimitePagoDao notificacionLimitePagoDao, EstadoCuentaServicio esperadaServicio,
            RenesServicio renesServicio, ReprocesoCargaServicio reprocesoCargaServicio,
            ReprocesoAfiliadoServicio reprocesoAfiliadoServicio,
            ControlEstadoCuentaServicio controlEstadoCuentaServicio, ActorSystem actorSystem,
            Materializer materializer) {

        this.afiliadosCoberturaDao = afiliadosCoberturaDao;
        this.controlNovedadesDao = controlNovedadesDao;
        this.cambiosEstadoCuentaDao = cambiosEstadoCuentaDao;
        this.controlIntegradorDao = controlIntegradorDao;
        this.novedadesDao = novedadesDao;
        this.parametrosDao = parametrosDao;
        this.notificacionLimitePagoDao = notificacionLimitePagoDao;
        this.esperadaServicio = esperadaServicio;
        this.renesServicio = renesServicio;
        this.reprocesoAfiliadoServicio = reprocesoAfiliadoServicio;
        this.reprocesoCargaServicio = reprocesoCargaServicio;
        this.controlEstadoCuentaServicio = controlEstadoCuentaServicio;
        this.materializer = materializer;
        this.actorSystem = actorSystem;
    }

    @Transactional
    public void registrarAfiliado(IntegradorEsperadaAfiliado integradorEsperada) {
        LOG.debug("************** REGISTRO AFILIADO");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), integradorEsperada.getTipoAfiliado(),
                integradorEsperada.getTipoCotizante(), integradorEsperada.getCertificado());

        Periodo inicio = Periodo.from(afiliado.getCobertura().getFealta());
        Periodo fin = Periodo.from(afiliado.getCobertura().getFebaja());

        proceso(integradorEsperada, inicio, fin, afiliado);
    }

    @Transactional
    private void proceso(IntegradorEsperada integradorEsperada, Periodo periodoInicial, Periodo periodoFinal,
            Afiliado afiliado) {
        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
        generarPeriodos(integradorEsperada, periodoInicial, periodoFinal, afiliado, reproceso);
        controlIntegrador(afiliado.getCobertura().getPoliza(), periodoInicial, periodoFinal, integradorEsperada);
    }

    private List<Afiliado> getPeridosAfiliado(final Periodo periodoInicial, final Periodo periodoFinal,
            final Afiliado afiliado) {

        Periodo periodoActual = Periodo.now();
        Periodo periodoInicio = periodoInicial;
        Periodo periodoFin = periodoFinal;

        if (periodoActual.compareTo(periodoFin) == 0) {
            if (String.valueOf('v')
                    .compareToIgnoreCase(UtilCadenas.trim(afiliado.getCondicion().getTipoGeneracion())) == 0) {
                periodoFin = periodoFin.minusMonths(1l);
            }
        }

        List<Periodo> periodos = Periodo.getPeridos(periodoInicio, periodoFin);
        String poliza = afiliado.getCobertura().getPoliza();

        List<Afiliado> afiliados = periodos.stream().map(periodo -> {

            Cobertura cobertura = afiliado.getCobertura().cloneCobertura();
            cobertura.setPoliza(poliza);
            cobertura.setPeriodoGeneracion(periodoActual.toString());
            cobertura.setPeriodo(periodo.format("MMyyyy"));

            Periodo feAlta = Periodo.from(cobertura.getFealta());
            cobertura.setEsMismoPeriodoDeAlta(feAlta.isEqual(periodo) ? "S" : "N");
            cobertura.setPeriodoEsMenorFealta(feAlta.isAfter(periodo) ? "S" : "N");

            Legalizacion legalizacion = afiliado.getLegalizacion().cloneLegalizacion();

            Afiliado afld = new Afiliado();
            afld.setDni(afiliado.getDni());
            afld.setCobertura(cobertura);
            afld.setTipoAfiliado(afiliado.getTipoAfiliado());
            afld.setTipoCotizante(afiliado.getTipoCotizante());
            afld.setCertificado(afiliado.getCertificado());
            afld.setSalario(afiliado.getSalario());
            afld.setTipoError(afiliado.getTipoError());
            afld.setUltimoIbc(afiliado.getUltimoIbc());
            afld.setTipoDocumentoEmpleador(afiliado.getTipoDocumentoEmpleador());
            afld.setDniEmpleador(afiliado.getDniEmpleador());

            afld.setCondicion(afiliado.getCondicion().cloneCondicion());
            afld.getCondicion().setPeriodoCotizacion(periodo.toString());
            afld.setLegalizacion(legalizacion);

            afld.setSubtipoCotizante(afiliado.getSubtipoCotizante());
            afld.setNmroCoberturas(afiliado.getNmroCoberturas());
            afld.setCsvTiposCotizantes(afiliado.getCsvTiposCotizantes());

            afld.setPeriodoCotizacion(periodo.format("MMyyyy"));

            return afld;
        }).collect(Collectors.toList());

        periodos.clear();
        periodos = null;

        return afiliados;
    }

    @Transactional
    public void cambiarActividadIndependiente(IntegradorEsperadaCambioActividad integradorEsperada) {

        LOG.debug("************** CAMBIAR ACTIVIDAD INDEPENDIENTE");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), IntegradorEsperadaCambioActividad.TIPO_AFILIADO_INDEPENDIENTE,
                null, null);

        Periodo inicio = Periodo.parse(integradorEsperada.getPeriodoInicial(), "yyyyMM");
        Periodo fin = Periodo.parse(integradorEsperada.getPeriodoFinal(), "yyyyMM");

        this.proceso(integradorEsperada, inicio, fin, afiliado);

        LOG.info("FIN  - Cambiar actividad independiente");
    }

    @Transactional
    public void reversarLegalizacion(IntegradorEsperadaReversaLegalizacion integradorEsperada) {
        LOG.debug("************** REVERSA LEGALIZACION");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        // Obtenemos los afiliados que fueron reversados
        CompletableFuture<List<Afiliado>> futuroAfiliados = CompletableFuture.supplyAsync(
                () -> afiliadosCoberturaDao.consultarAfiliadosReversados(integradorEsperada.getNmformulario()));

        futuroAfiliados.whenComplete((afiliados, ex) -> {

            LOG.info("Afiliados -> {}  ", afiliados.size());

            int renes = renesServicio.borrarSinCobertura(integradorEsperada.getPoliza(), null,
                    integradorEsperada.getPeriodo(), integradorEsperada.getNmformulario());
            LOG.debug("Renes borrados {}", renes);

            List<CompletableFuture<Done>> futuros = afiliados.stream()
                    .map(afiliado -> procesarReversaLegalizacion(integradorEsperada, afiliado).toCompletableFuture())
                    .collect(Collectors.toList());

            CompletableFuture allOf = CompletableFuture.allOf(futuros.toArray(new CompletableFuture[futuros.size()]))
                    .whenComplete((done, u) -> {
                        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(),
                                EstadoIntegrador.PROCESADO, integradorEsperada.getDniUsuario());
                        LOG.info("FIN REVERSA LEGALIZACION");
                    });
            // allOf.join();
            LOG.info("FIN REVERSA LEGALIZACION");
        });

    }

    private CompletionStage<Done> procesarReversaLegalizacion(IntegradorEsperadaReversaLegalizacion integradorEsperada,
            Afiliado afiliado) {

        Periodo periodo = Periodo.parse(afiliado.getPeriodoCotizacion(), "yyyyMM");
        LOG.debug("************** REVERSA LEGALIZACION {} - {}", afiliado.getDni(), periodo);

        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
        controlIntegrador(integradorEsperada.getPoliza(), periodo, periodo, integradorEsperada);
        this.esperadaServicio.borrar(afiliado, periodo.toString(), periodo.toString(), integradorEsperada.getDniUsuario());

        return generarPeriodos(integradorEsperada, periodo, periodo, afiliado, reproceso);
    }

    @Transactional
    public void borrarAfiliado(IntegradorEsperadaAfiliado integradorEsperada) {

        LOG.info("ENTRE A BORRAR AFILIADO CON {} ", integradorEsperada);
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        String tipoCotizante = afiliadosCoberturaDao.getTipoCotizanteAnulado(integradorEsperada.getPoliza(),
                integradorEsperada.getTipoAfiliado(), integradorEsperada.getCertificado(),
                integradorEsperada.getDniAfiliado());
        LOG.debug("ENTRE A BORRAR CON TIPO COTIZANTE {} {} ", integradorEsperada.getDniAfiliado(), tipoCotizante);
        Afiliado afiliado = new Afiliado();
        afiliado.setCobertura(new Cobertura(integradorEsperada.getPoliza()));
        afiliado.setDni(integradorEsperada.getDniAfiliado());
        afiliado.setTipoAfiliado(integradorEsperada.getTipoAfiliado());
        afiliado.setTipoCotizante(tipoCotizante);
        afiliado.getCobertura().setFealta(integradorEsperada.getFechaAlta());
        afiliado.getCobertura().setFebaja(integradorEsperada.getFechaBaja());

        Periodo periodoFechaAlta = Periodo.from(integradorEsperada.getFechaAlta());
        Periodo periodoFechaBaja = Periodo.from(integradorEsperada.getFechaBaja());

//        afiliadosCoberturaDao.consultarCondicionesCotizante(afiliado.getTipoCotizante(), afiliado.getTipoAfiliado())
//                .ifPresent(condiciones -> afiliado.setCondicion(condiciones));
        LOG.info("periodoFechaAlta de borrado {} ", periodoFechaAlta);
        LOG.info("periodoFechaBaja de borrado {} ", periodoFechaBaja);

        esperadaServicio.borrar(afiliado, periodoFechaBaja.toString(), periodoFechaAlta.toString(),
                integradorEsperada.getDniUsuario());

        Periodo.getPeridos(periodoFechaAlta, periodoFechaBaja).forEach((pe) -> {
            afiliado.getCobertura().setPeriodo(pe.format("MMyyyy"));
            this.actualizarConsolidador(afiliado);
        });
        /*
		 * Se consulta el numero de periodos a borrar en estado de cuenta. Si ingreso
		 * aca es porque fisicamente se borro una cobertura, si el resultado es mayor
		 * igual a 1 es porque tenia multiplecobertura, sino solo tenía una.
         */
        List<String> periodosReCalcularReprocesar = afiliadosCoberturaDao.consultarPeriodosReCalcularEstadoCuenta(
                integradorEsperada.getPoliza(), integradorEsperada.getDniAfiliado(),
                integradorEsperada.getTipoAfiliado(), integradorEsperada.getTipoCotizante(),
                periodoFechaAlta.toString(), periodoFechaBaja.toString());

        LOG.info("NUMERO DE PERIODOS A RECALCULAR TAMANIO {} ", periodosReCalcularReprocesar.size());

        if (periodosReCalcularReprocesar.isEmpty()) {
            // Consultamos los pagos que hubiera tenido el afiliado y se
            // registra rene
            buscarFormulariosParaRene(afiliado, null);
        } else {
            periodosReCalcularReprocesar.forEach(periodo -> {
                LOG.info("El periodo es {} ", periodo);
                Periodo prd = Periodo.parse(periodo, "yyyyMM");
                Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
                generarPeriodos(integradorEsperada, prd, prd, afiliado, reproceso);
            });
        }
        controlIntegrador(integradorEsperada.getPoliza(), periodoFechaAlta, periodoFechaBaja, integradorEsperada);
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.PROCESADO,
                integradorEsperada.getDniUsuario());
    }

    @Transactional
    public void retirarAfiliado(IntegradorEsperadaAfiliado integradorEsperada) {
        LOG.debug("************** RETIRO");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());
        /*
		 * - buscar afiliado - validar si hay que reprocesar - borrar otros periodos -
		 * buscar el numero de formulario basado en ese periodo. (no tener en cuenta los
		 * estados). - generar rne
         */
        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), integradorEsperada.getTipoAfiliado(),
                integradorEsperada.getTipoCotizante(), integradorEsperada.getCertificado());

        Periodo fechaBaja = Periodo.from(afiliado.getCobertura().getFebaja());
        Periodo ultimoPeriodo = Periodo.from(esperadaServicio.getUltimoPeriodoGenerado(afiliado));
        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
        if (ultimoPeriodo != null) {
            if (fechaBaja.compareTo(ultimoPeriodo) >= 0) {
                generarPeriodos(integradorEsperada, ultimoPeriodo, fechaBaja, afiliado, reproceso);
            } else {
                Periodo fechaInicial = fechaBaja.isEqual(Periodo.from(afiliado.getCobertura().getFealta())) ? fechaBaja
                        : fechaBaja.plusMonths(1l);
                borrarEstadoCuenta(integradorEsperada, fechaInicial, ultimoPeriodo, afiliado);
                generarPeriodos(integradorEsperada, fechaBaja, fechaBaja, afiliado, reproceso);
            }
            controlIntegrador(integradorEsperada.getPoliza(), fechaBaja, ultimoPeriodo, integradorEsperada);
        } else {
            LOG.info("No se encontro periodo en estado de cuenta");
            cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.PROCESADO,
                    integradorEsperada.getDniUsuario());
        }

        LOG.info("FIN  - Borrando");
    }

    @Transactional
    public void movimientoCoberturaAfiliado(IntegradorEsperadaCobertura integradorEsperada) {

        LOG.debug("************** MOVIMIENTO COBERTURA");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());
        /*
		 * - consultar afiliado - consultar fecha de baja y alta - determinar los
		 * periodos faltantes + generar estado de cuenta(esperada), validar si tiene
		 * pagos y reprocesar. - determinar periodos sobrantes + se borran y se generan
		 * renes dado el caso
		 * 
         */
        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), integradorEsperada.getTipoAfiliado(),
                integradorEsperada.getTipoCotizante(), integradorEsperada.getCertificado());

        Periodo fechaAfiliacion = Periodo.from(afiliado.getCobertura().getFealta());
        Periodo fechaRetiro = Periodo.from(afiliado.getCobertura().getFebaja());
        Periodo fechaActual = Periodo.now();

        Periodo fechaAltaAnterior = integradorEsperada.getFechaAltaAnterior() != null
                ? Periodo.from(integradorEsperada.getFechaAltaAnterior())
                : null;
        Periodo fechaBajaAnterior = integradorEsperada.getFechaBajaAnterior() != null
                ? Periodo.from(integradorEsperada.getFechaBajaAnterior())
                : null;

        LOG.debug("Fecha alta" + fechaAltaAnterior + " - " + fechaAfiliacion);
        LOG.debug("Fecha baja" + fechaBajaAnterior + " - " + fechaActual);

        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
        // obtener la fecha mas baja y buscar desde esa fecha
        // obtener la fecha mas alta y buscar hasta esa
        Periodo fechaBusqueda;
        if (fechaAltaAnterior != null) {

            fechaBusqueda = fechaAltaAnterior.isBefore(fechaAfiliacion) ? fechaAltaAnterior : fechaAfiliacion;
            Periodo primerPeriodo = Periodo
                    .from(esperadaServicio.getPrimerPeriodoGenerado(afiliado, fechaBusqueda.firstDayOfMonth()));

            if (primerPeriodo == null) {
                LOG.debug("primerPeriodo no existe");
                generarPeriodos(integradorEsperada, fechaAfiliacion, fechaActual, afiliado, reproceso);
            } else if (primerPeriodo.isBefore(fechaAfiliacion)) {
                LOG.debug("primerPeriodo antes de fecha afiliacion");
                borrarEstadoCuenta(integradorEsperada, primerPeriodo, fechaAfiliacion, afiliado);
                generarPeriodos(integradorEsperada, fechaAfiliacion, fechaAfiliacion, afiliado, reproceso);
            } else if (primerPeriodo.isAfter(fechaAfiliacion)) {
                LOG.debug("primerPeriodo despues de fecha afiliacion");
                generarPeriodos(integradorEsperada, fechaAfiliacion, primerPeriodo, afiliado, reproceso);
            } else {
                LOG.debug("Validar fecha de afiliacion movimiento cobertura.");
                generarPeriodos(integradorEsperada, fechaAfiliacion, fechaAfiliacion, afiliado, reproceso);
            }
        }

        if (fechaBajaAnterior != null) {

            fechaBusqueda = fechaBajaAnterior.isBefore(fechaRetiro) ? fechaRetiro : fechaBajaAnterior;
            LOG.debug("buscar antes de:" + fechaBusqueda);
            Periodo ultimoPeriodo = Periodo
                    .from(esperadaServicio.getUltimoPeriodoGenerado(afiliado, fechaBusqueda.lastDayOfMonth()));
            if (ultimoPeriodo == null) {
                LOG.debug("ultimoPeriodo no existe");
                generarPeriodos(integradorEsperada, fechaAfiliacion, fechaActual, afiliado, reproceso);
            } else if (ultimoPeriodo.isBefore(fechaRetiro)) {
                LOG.debug("ultimoPeriodo antes de fecha retiro");
                if (ultimoPeriodo.isBefore(fechaActual)) {
                    LOG.debug("*ultimoPeriodo antes de fecha actual");
                    generarPeriodos(integradorEsperada, ultimoPeriodo, fechaActual, afiliado, reproceso);
                } else {
                    LOG.debug("No hacer nada");
                }
            } else if (ultimoPeriodo.isAfter(fechaRetiro)) {
                LOG.debug("ultimoPeriodo despues de fecha retiro");
                borrarEstadoCuenta(integradorEsperada, fechaRetiro.plusMonths(1L), fechaActual, afiliado);
                generarPeriodos(integradorEsperada, fechaRetiro, fechaRetiro, afiliado, reproceso);
            } else {
                LOG.debug("Validar fecha de retiro movimiento cobertura.");
                borrarEstadoCuenta(integradorEsperada, fechaRetiro.plusMonths(1L), fechaActual, afiliado);
                generarPeriodos(integradorEsperada, fechaRetiro, fechaRetiro, afiliado, reproceso);
            }
        }
        controlIntegrador(integradorEsperada.getPoliza(), fechaRetiro, fechaActual, integradorEsperada);
        LOG.info("FIN  - Movimiento Cobertura");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.PROCESADO,
                integradorEsperada.getDniUsuario());
    }

    @Transactional
    public void cambiarCentroTrabajo(IntegradorEsperadaCentroTrabajo integradorEsperada) {
        LOG.debug("************** CENTRO TRABAJO");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), integradorEsperada.getTipoAfiliado(),
                integradorEsperada.getTipoCotizante(), integradorEsperada.getCertificado());

        Periodo inicio = Periodo.parse(integradorEsperada.getPeriodoInicial(), "yyyyMM");
        Periodo fin = Periodo.parse(integradorEsperada.getPeriodoFinal(), "yyyyMM");

        this.proceso(integradorEsperada, inicio, fin, afiliado);
//        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
//        generarPeriodos(integradorEsperada, inicio, fin, afiliado, reproceso);
//        controlIntegrador(integradorEsperada.getPoliza(), inicio, fin, integradorEsperada);
        LOG.info("FIN  - Cambiar centro de trabajo");
    }

    @Transactional
    public void cambiarTasa(IntegradorEsperadaCambioTasa integradorEsperada) {
        LOG.debug("************** CAMBIAR TASA");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());
        /*
		 * - Consultar afiliados - generar periodos - generar esperadas - actualizar
         */

        List<Afiliado> afiliados_cambiar_tasa = afiliadosCoberturaDao.consultarAfiliadoPorCentroTrabajo(
                integradorEsperada.getPoliza(), integradorEsperada.getCentroTrabajo(),
                integradorEsperada.getActividad(), integradorEsperada.getClase());
        LOG.debug("Cantidad de afiliados {} por procesar {} ", integradorEsperada.getPoliza(),
                afiliados_cambiar_tasa.size());
        if (afiliados_cambiar_tasa.isEmpty()) {
            cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.PROCESADO,
                    integradorEsperada.getDniUsuario());
        }

        Periodo inicio = Periodo.parse(integradorEsperada.getPeriodoInicial(), "yyyyMM");
        Periodo finall = Periodo.parse(integradorEsperada.getPeriodoFinal(), "yyyyMM");
        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
        afiliados_cambiar_tasa.stream().forEach((afiliado) -> {
            generarPeriodos(integradorEsperada, inicio, finall, afiliado, reproceso);
        });
        controlIntegrador(integradorEsperada.getPoliza(), inicio, finall, integradorEsperada);
        LOG.info("FIN  - Cambiar Tasa");
    }

    @Transactional
    private void borrarEstadoCuenta(IntegradorEsperada ie, Periodo periodoInicial, Periodo periodoFinal,
            Afiliado afiliado) {
        LOG.info("BORRAR PERIODOS {} {} {}", periodoInicial, periodoFinal, afiliado.getDni());

        List<Periodo> periodos = Periodo.getPeridos(periodoInicial, periodoFinal);
        LOG.info("Buscando Rene para {} periodos", periodos.size());

        List<String> periodosCobertura = esperadaServicio.getPeriodosCobertura(afiliado, periodoInicial.toDate(),
                periodoFinal.toDate());

        Consumer<Afiliado> reproceso = (af) -> reproceso(af, ie.getDniUsuario());
        periodos.stream().forEach((periodo) -> {
            if (periodosCobertura.contains(periodo.toString())) {
                generarPeriodos(ie, periodo, periodo, afiliado, reproceso);
            } else {
                afiliado.getCobertura().setPeriodo(periodo.format("MMyyyy"));
                actualizarConsolidador(afiliado);
                esperadaServicio.borrar(afiliado, periodo.toString(), periodo.toString(), ie.getDniUsuario());
                buscarFormulariosParaRene(afiliado, periodo.toString());
                // reproceso.accept(afiliado);
            }
        });
    }

    private CompletionStage<Done> generarPeriodos(IntegradorEsperada ie, Periodo periodoInicial, Periodo periodoFinal,
            Afiliado afiliado, Consumer<Afiliado> reproceso) {

        Map<String, Date> fechaLimite = new HashMap<>();

        LOG.info("GENERAR PERIODOS {} {} {} {}", periodoInicial, periodoFinal, afiliado.getDni(), afiliado.getCsvTiposCotizantes());

        Periodo fealta = Periodo.from(afiliado.getCobertura().getFealta());
        Periodo febaja = Periodo.from(afiliado.getCobertura().getFebaja());
        Periodo fin = Periodo.now();
        fin = fin.compareTo(periodoFinal) >= 0 ? periodoFinal : fin;
        fin = fin.compareTo(febaja) >= 0 ? febaja : fin;
        Periodo inicial = periodoInicial.compareTo(fealta) >= 0 ? periodoInicial : fealta;
        Periodo corte = getPeriodoCorte();
        inicial = corte.compareTo(inicial) >= 0 ? corte : inicial;

        List<Afiliado> afiliados = this.getPeridosAfiliado(inicial, fin, afiliado);

        if (afiliados.isEmpty()) {
            LOG.info("No se generan periodos para este afiliado: {}", afiliado.getDni());
            cambiosEstadoCuentaDao.actualizarRegistroATramitar(ie.getId(), EstadoIntegrador.PROCESADO,
                    ie.getDniUsuario());
//            return;
        }

        LOG.info("Periodos generados:{} ", afiliados.size());

        Source<EstadoCuenta, NotUsed> resultado = Source.from(new ArrayList<>(afiliados))
                .mapAsyncUnordered(CANTIDAD_MENSAJES, (t) -> {
                    return CompletableFuture.supplyAsync(() -> {
                        return esperadaServicio.calcularEstadoCuenta(t);
                    });
                });

        CompletionStage<Done> proceso = resultado.withAttributes(ActorAttributes.withSupervisionStrategy(e -> {
            LOG.error("Se presento un error: {} ", e.getMessage());
            return Supervision.resume();
        })).runWith(Sink.foreach(eCUenta -> {
            try {
                eCUenta.getAfiliado().setUsuarioOperacion(ie.getDniUsuario());
                eCUenta.setUsuarioOperacion(ie.getDniUsuario());

                try {
                    Date fechaLimitePago = getFechaLimitePago(eCUenta.getAfiliado().getCobertura().getPoliza(),
                            eCUenta.getAfiliado().getCobertura().getPeriodoAnioMes(), fechaLimite);

                    controlEstadoCuenta(eCUenta, fechaLimitePago);

                    eCUenta.setFechaLimitePago(fechaLimitePago);
                } catch (Exception n) {
                    LOG.error("Error obteniendo fecha limite de pago a:" + eCUenta.getAfiliado(), n);
                }

                esperadaServicio.merge(eCUenta);
                // Reprocesar
                reproceso.accept(eCUenta.getAfiliado());

                LOG.info("Registrando estado de cuenta!!!!!:"
                        + eCUenta.getAfiliado().getCondicion().getPeriodoCotizacion() + " - " + eCUenta.getDias()
                        + " - " + eCUenta.getTasa() + " - " + eCUenta.getAfiliado().getTipoCotizante());
                LOG.info("Removio afiliado:{}", afiliados.remove(eCUenta.getAfiliado()));
            } catch (Exception e) {
                LOG.error("Error registrando estado de cuenta!!!!!:" + eCUenta.getAfiliado(), e);
            }

        }), materializer);

        return proceso.whenComplete((done, e) -> {
            if (e != null) {
                LOG.error(e.getMessage(), e);
                cambiosEstadoCuentaDao.actualizarRegistroATramitar(ie.getId(), EstadoIntegrador.ERROR_PROCESO,
                        ie.getDniUsuario());
            } else {
                LOG.info("Falto por procesar:{}", afiliados.size());
                LOG.info("Integracion realizada con exito");
                cambiosEstadoCuentaDao.actualizarRegistroATramitar(ie.getId(), EstadoIntegrador.PROCESADO,
                        ie.getDniUsuario());
            }
            afiliados.clear();
        });
    }

    private void controlEstadoCuenta(EstadoCuenta eCUenta, Date fechaLimitePago) {

        controlEstadoCuentaServicio.crearControlEstadoCuenta(
                eCUenta.getAfiliado().getCobertura().getPoliza(),
                eCUenta.getAfiliado().getCobertura().getPeriodoAnioMes(), Periodo.now().toString(),
                TipoAfiliadoControl.DEPENDIENTE,
                0, fechaLimitePago);

        controlEstadoCuentaServicio.crearControlEstadoCuenta(
                eCUenta.getAfiliado().getCobertura().getPoliza(),
                eCUenta.getAfiliado().getCobertura().getPeriodoAnioMes(), Periodo.now().toString(),
                TipoAfiliadoControl.INDEPENDIENTE,
                0, fechaLimitePago);

        controlEstadoCuentaServicio.crearControlEstadoCuenta(
                eCUenta.getAfiliado().getCobertura().getPoliza(),
                eCUenta.getAfiliado().getCobertura().getPeriodoAnioMes(), Periodo.now().toString(),
                TipoAfiliadoControl.ESTUDIANTE,
                0, fechaLimitePago);
    }

    private void reproceso(Afiliado afiliado, String modifica) {
        IntegradorEsperadaAfiliadoServicio.this.reproceso(null, afiliado, modifica);
    }

    private void reproceso(Periodo periodo, Afiliado afiliado, String modifica) {

        if (periodo != null) {
            afiliado.getCondicion().setPeriodoCotizacion(periodo.toString());
            afiliado.getCobertura().setPeriodoGeneracion(Periodo.now().toString());
            afiliado.getCobertura().setPeriodo(periodo.format("MMyyyy"));
            afiliado.setPeriodoCotizacion(periodo.toString());
        }

        LOG.info("VALIDAR PERIODO {} {}", afiliado.getCobertura().getPeriodo(), afiliado.getDni());

        try {
            InfoNovedadVCT vct = novedadesDao.getNovedadVCT(afiliado.getDni().substring(1),
                    afiliado.getCobertura().getPeriodo(), afiliado.getCobertura().getPoliza(),
                    afiliado.getTipoCotizante());
            if (vct != null) {
                LOG.debug("InfoVCT para {} {}", afiliado.getCobertura().getPeriodo(), afiliado.getDni());
            }
            afiliado.setInfoVct(vct);
        } catch (Exception q) {
            LOG.debug("FALLO InfoVCT para {} {}", afiliado.getCobertura().getPeriodo(), afiliado.getDni());
            LOG.error(q.getMessage());
            LOG.error(q.getLocalizedMessage(), q);
        }

        try {
            reprocesoAfiliadoServicio.ejecutarRecalculo(afiliado);

        } catch (Exception w) {
            LOG.debug("FALLO reproceso para {} {}", afiliado.getCobertura().getPeriodo(), afiliado.getDni());
            LOG.error(w.getMessage());
            LOG.error(w.getLocalizedMessage(), w);
        }

        this.actualizarConsolidador(afiliado);

        LOG.info("Reproceso para {} {}", afiliado.getCobertura().getPeriodo(), afiliado.getDni());
    }

    private void actualizarConsolidador(Afiliado afiliado) {
        try {
            reprocesoCargaServicio.enviarMensajeConsolidador(afiliado, Optional.empty());
        } catch (Exception w) {
            LOG.debug("FALLO consolidado para {} {}", afiliado.getCobertura().getPeriodo(), afiliado.getDni());
            LOG.error(w.getMessage());
            LOG.error(w.getLocalizedMessage(), w);
        }
    }

    private void buscarFormulariosParaRene(Afiliado afiliado, String periodo) {

        List<Long> numeroFormulario = controlNovedadesDao.getNumerosFormulario(afiliado.getCobertura().getPoliza(),
                afiliado.getDni(), periodo, afiliado.getTipoAfiliado());
        LOG.info("Formularios encontrados para el dni {} y periodo {} son {}", afiliado.getDni(), periodo,
                numeroFormulario.size());
        actualizarConsolidador(afiliado);
        numeroFormulario.forEach((numFormulario) -> {

            LOG.debug("nf:" + numFormulario + " tc:" + afiliado.getTipoCotizante() + "ta:" + afiliado.getTipoAfiliado()
                    + " dni:" + afiliado.getDni());

            Integer renesRegistrados = renesServicio.registrarRene(numFormulario, afiliado.getDni(),
                    afiliado.getTipoCotizante(), afiliado.getTipoAfiliado());
            if (renesRegistrados > 0) {
                LOG.info("RNE Creado por retiro para afiliado {}", afiliado.getDni());
            } else {
                LOG.info("RNE NO Creado por retiro para afiliado {}", afiliado.getDni());
            }
        });
    }

    @Transactional
    public void cambioDocumento(IntegradorEsperadaCambioDocumento integradorEsperada) {

        LOG.debug("************** CAMBIO DOCUMENTO");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        List<Afiliado> afiliados = afiliadosCoberturaDao.consultarAfiliado(null, integradorEsperada.getDniAfiliado());

        Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());

        afiliados.stream().forEach((afiliado) -> {
            Periodo inicio = Periodo.from(afiliado.getCobertura().getFealta());
            Periodo fin = Periodo.from(afiliado.getCobertura().getFebaja());
            controlIntegrador(afiliado.getCobertura().getPoliza(), inicio, fin, integradorEsperada);
            generarPeriodos(integradorEsperada, inicio, fin, afiliado, reproceso);
        });

        LOG.info("FIN  - Cambio Documento");
    }

    @Transactional
    public void novedadAfiliado(IntegradorEsperadaAfiliado integradorEsperada) {

        LOG.debug("************** NOVEDAD " + integradorEsperada.getSubtipo());
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());
        Periodo periodo = Periodo.parse(integradorEsperada.getPeriodo(), "yyyyMM");
        List<Afiliado> afiliados;

        if ("carga_novedades_pago".compareToIgnoreCase(integradorEsperada.getSubtipo()) == 0) {

//            afiliados = novedadesDao.obtenerAfiliadosXformulario(
//                    Long.valueOf(integradorEsperada.getFormularioPago()),
//                    Optional.empty(),
//                    Optional.empty());
            afiliados = afiliadosCoberturaDao.consultarAfiliadosNoReversados(integradorEsperada.getFormularioPago(), null, periodo.format("MMyyyy"),null);

        } else if ("borrar_novedad_pago".compareToIgnoreCase(integradorEsperada.getSubtipo()) == 0) {
            afiliados = afiliadosCoberturaDao.consultarAfiliadosReversados(integradorEsperada.getFormularioPago(),
                    integradorEsperada.getDniAfiliado().substring(1), periodo.format("MMyyyy"),integradorEsperada.getDniAfiliado().substring(0,1));
            if (afiliados.isEmpty()) {
                borrarRenesSinCobertura(integradorEsperada);
                if (integradorEsperada.getTipoCotizante() != null) {
                    LOG.debug("Buscar afiliado para enriques. {}", periodo);
                    Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                            integradorEsperada.getDniAfiliado(), integradorEsperada.getTipoAfiliado(),
                            integradorEsperada.getTipoCotizante(), integradorEsperada.getCertificado());
                    LOG.debug("Encontro afiliado para enriques. {}", afiliado != null);
                    if (afiliado != null) {
                        this.esperadaServicio.borrar(afiliado, periodo.toString(), periodo.toString(), integradorEsperada.getDniUsuario());
                        this.proceso(integradorEsperada, periodo, periodo, afiliado);
                        LOG.debug("Se marco Enrique");
                    } else {
                        LOG.debug("NO se marco Enrique");
                    }
                }
            }
        } else if ("actualizar_tipo_resolucion_pago".compareToIgnoreCase(integradorEsperada.getSubtipo()) == 0) {
            afiliados = afiliadosCoberturaDao.consultarAfiliadosNoReversados(integradorEsperada.getFormularioPago(),
                    null, periodo.format("MMyyyy"),null);
            LOG.debug("AFILIADOS R {}", afiliados.size());
        } else {

            afiliados = afiliadosCoberturaDao.consultarAfiliadosNoReversados(integradorEsperada.getFormularioPago(),
                    integradorEsperada.getDniAfiliado().substring(1), periodo.format("MMyyyy"), integradorEsperada.getDniAfiliado().substring(0,1));

            if (afiliados.isEmpty()
                    && "actualizar_novedad_pago".compareToIgnoreCase(integradorEsperada.getSubtipo()) == 0) {
                List<Afiliado> noAfiliados = novedadesDao.obtenerAfiliadosXformulario(
                        Long.valueOf(integradorEsperada.getFormularioPago()),
                        Optional.of(integradorEsperada.getDniAfiliado().substring(1)),
                        Optional.of(periodo.format("MMyyyy")));
                LOG.debug("No AFILIADOS {}", noAfiliados.size());
                noAfiliados.forEach((afiliado) -> {
                    int rene = renesServicio.registrarRene(Long.valueOf(integradorEsperada.getFormularioPago()),
                            integradorEsperada.getDniAfiliado(), afiliado.getTipoCotizante(),
                            afiliado.getTipoAfiliado());
                    LOG.debug("RENE {}", rene);
                    actualizarConsolidador(afiliado);
                });
                cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(),
                        EstadoIntegrador.PROCESADO, integradorEsperada.getDniUsuario());
            }
        }
        LOG.debug("AFILIADOS {}", afiliados.size());

        List<CompletableFuture<Done>> futuros = afiliados.stream().map(afiliado -> {
            this.esperadaServicio.borrar(afiliado, periodo.toString(), periodo.toString(), integradorEsperada.getDniUsuario());

            registrarReneNovedad(periodo, afiliado, integradorEsperada);

            afiliado.getLegalizacion().setNumeroFormulario(Long.valueOf(integradorEsperada.getFormularioPago()));
            Consumer<Afiliado> reproceso = (af) -> reproceso(af, integradorEsperada.getDniUsuario());
            return generarPeriodos(integradorEsperada, periodo, periodo, afiliado, reproceso).toCompletableFuture();
        }).collect(Collectors.toList());

        CompletableFuture allOf = CompletableFuture.allOf(futuros.toArray(new CompletableFuture[futuros.size()]))
                .whenComplete((done, u) -> {
                    cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(),
                            EstadoIntegrador.PROCESADO, integradorEsperada.getDniUsuario());
                    LOG.info("FIN  - Novedad");
                });
        // allOf.join();

        controlIntegrador(integradorEsperada.getPoliza(), periodo, periodo, integradorEsperada);
        LOG.info("FIN  - Novedad");
    }

    private void registrarReneNovedad(Periodo periodo, Afiliado afiliado, IntegradorEsperadaAfiliado integradorEsperada) throws NumberFormatException {
        try {
            boolean registrarRene = false;

            if (afiliado.getCobertura() == null) {
                registrarRene = true;
            } else {
                Periodo fealta = Periodo.from(afiliado.getCobertura().getFealta());
                Periodo febaja = Periodo.from(afiliado.getCobertura().getFebaja());

//            LOG.info(""+(fealta.compareTo(periodo) < 0 && periodo.compareTo(febaja) < 0));
//            LOG.info("fealta"+(fealta.compareTo(periodo) < 0 ));
//            LOG.info("febaja"+ (periodo.compareTo(febaja) < 0));
                if (!(fealta.compareTo(periodo) < 0 && periodo.compareTo(febaja) < 0)) {
                    registrarRene = true;
                }
            }

            if (registrarRene) {
                LOG.debug("RENE para {} {} {} {}", afiliado.getDni(), integradorEsperada.getFormularioPago(), afiliado.getTipoCotizante(), afiliado.getTipoAfiliado());
                int rene = renesServicio.registrarRene(Long.valueOf(integradorEsperada.getFormularioPago()),
                        afiliado.getDni(), null, null);
                LOG.debug("RENE {}", rene);
            }
        } catch (Exception e) {
            LOG.debug("Fallo Renes {}", afiliado.getDni());
            LOG.debug("Fallo Renes", e);
        }
    }

    private void borrarRenesSinCobertura(IntegradorEsperadaAfiliado integradorEsperada) {
        int renes = renesServicio.borrarSinCobertura(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), integradorEsperada.getPeriodo(),
                integradorEsperada.getFormularioPago());
        if (renes > 0) {
            Afiliado a = new Afiliado();
            a.setCobertura(new Cobertura(integradorEsperada.getPoliza(), Periodo.parse(integradorEsperada.getPeriodo(), "yyyyMM").format("MMyyyy")));
            actualizarConsolidador(a);
        }
        LOG.debug("Renes borrados {}", renes);
    }

    private void controlIntegrador(String contrato, Periodo inicio, Periodo fin,
            IntegradorEsperada integradorEsperada) {

        String fuente = cambiosEstadoCuentaDao.getFuente(integradorEsperada.getId());
        CambioIntegrador cambioIntegrador = CambioIntegrador.ERROR;

        if (fuente != null) {
            switch (fuente) {
                case "01":
                    cambioIntegrador = CambioIntegrador.REVERSA_LEGALIZACION;
                    break;
                case "07":
                    cambioIntegrador = CambioIntegrador.INGRESO;
                    break;
                case "08":
                case "09":
                    cambioIntegrador = CambioIntegrador.MOVIMIENTO_COBERTURAS;
                    break;
                case "11":
                case "12":
                case "13":
                    cambioIntegrador = CambioIntegrador.RETIRO;
                    break;
                case "14":
                    cambioIntegrador = CambioIntegrador.CANCELACION;
                    break;
                default:
                    break;
            }
        } else {
            switch (integradorEsperada.getTipo()) {
                case ACTUALIZACIONNOVEDAD:
                    cambioIntegrador = CambioIntegrador.ACTUALIZACION_NOVEDAD;
                    break;
                case AFILIACION:
                    cambioIntegrador = CambioIntegrador.INGRESO;
                    break;
                case AFILIACION_IND:
                    cambioIntegrador = CambioIntegrador.INGRESO;
                    break;
                case ANULACION:
                    cambioIntegrador = CambioIntegrador.ANULACION_COBERTURAS;
                    break;
                case CAMBIO_ACTIVIDAD_IND:
                    cambioIntegrador = CambioIntegrador.MODIFICACION_CARGAS;
                    break;
                case CAMBIO_CENTRO_TRABAJO:
                    cambioIntegrador = CambioIntegrador.MODIFICACION_CARGAS;
                    break;
                case CAMBIO_CTP:
                    cambioIntegrador = CambioIntegrador.CAMBIO_TASA;
                    break;
                case CAMBIO_DOCUMENTO:
                    cambioIntegrador = CambioIntegrador.INGRESO;
                    break;
                case CAMBIO_TASA_CT:
                    cambioIntegrador = CambioIntegrador.CAMBIO_TASA;
                    break;
                case CANCELACION_CONTRATO:
                    cambioIntegrador = CambioIntegrador.CANCELACION;
                    break;
                case INDEPENDIENTES_VOLV:
                    cambioIntegrador = CambioIntegrador.MOVIMIENTO_COBERTURAS;
                    break;
                case MOVER_COBERTURA:
                    cambioIntegrador = CambioIntegrador.MOVIMIENTO_COBERTURAS;
                    break;
                case RETIRO:
                   cambioIntegrador = CambioIntegrador.RETIRO;
                    break;
                case REVERSA_LEGALIZACION:
                    cambioIntegrador = CambioIntegrador.REVERSA_LEGALIZACION;
                    break;
                case REPROCESO_FLUJO_COMPLETO:
                    cambioIntegrador = CambioIntegrador.INGRESO;
                    break;
            }
        }

        controlIntegrador(contrato, inicio, fin, cambioIntegrador, integradorEsperada.getDniUsuario());
    }

    private void controlIntegrador(String contrato, Periodo inicio, Periodo fin, CambioIntegrador cambioIntegrador,
            String dniIngreso) {

        List<ControlIntegrador> cis = new ArrayList<>();

        ControlIntegrador control = new ControlIntegrador();
        control.setPeriodo(inicio);
        control.setContrato(contrato);
        control.setDniIngreso(dniIngreso);
        control.setMotivoCambio(cambioIntegrador);
        cis.add(control);

        if (!inicio.isEqual(fin)) {
            ControlIntegrador control2 = new ControlIntegrador();
            control2.setPeriodo(fin);
            control2.setContrato(contrato);
            control2.setDniIngreso(dniIngreso);
            control2.setMotivoCambio(cambioIntegrador);
            cis.add(control2);
        }

        controlIntegradorDao.guardar(cis);
    }

    private Periodo getPeriodoCorte() {

//        Parametro pa = parametrosDao.consultar("integrador.periodo.arranque");
        String valor = parametrosDao.obtenerTodosParametros().get("integrador.periodo.arranque");

        return Periodo.parse(valor, "yyyyMM");
    }

    private Date getFechaLimitePago(String poliza, String periodo, Map<String, Date> temporal) {
        String key = poliza + "-" + periodo;
        if (temporal.containsKey(key)) {
            return temporal.get(key);
        }

        Optional<Date> n = notificacionLimitePagoDao.consultaFechaLimitePago(poliza, periodo);

        if (n.isPresent()) {
            temporal.put(key, n.get());
        }

        return n.orElse(null);
    }

    private TipoAfiliadoControl obtenerAfiliadoControl(String tipoAfiliado, String tipoCotizante) {

        if (TipoAfiliado.INDEPENDIENTE.getEquivalencia().equals(tipoAfiliado)) {
            return TipoAfiliadoControl.INDEPENDIENTE;
        }

        if (TipoAfiliado.EMPRESA.getEquivalencia().equals(tipoAfiliado)) {

            if (TipoCotizante.ESTUDIANTE.getEquivalencia().equals(tipoCotizante)) {
                return TipoAfiliadoControl.ESTUDIANTE;
            }

            return TipoAfiliadoControl.DEPENDIENTE;
        }

        return TipoAfiliadoControl.DEPENDIENTE;
    }
    
    @Transactional
    public void reprocesoFlujoCompleto(IntegradorEsperadaAfiliado integradorEsperada) {
        
        LOG.debug("************** REPROCESO_FLUJO_COMPLETO");
        cambiosEstadoCuentaDao.actualizarRegistroATramitar(integradorEsperada.getId(), EstadoIntegrador.ENPROCESO,
                integradorEsperada.getDniUsuario());

        /*Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(integradorEsperada.getPoliza(),
                integradorEsperada.getDniAfiliado(), integradorEsperada.getTipoAfiliado(),
                integradorEsperada.getTipoCotizante(), integradorEsperada.getCertificado());*/
        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliadosFlujoCompleto(integradorEsperada.getFormularioPago(), 
                integradorEsperada.getDniAfiliado().substring(1), integradorEsperada.getDniAfiliado().substring(0,1),integradorEsperada.getPeriodo()).get(0);

        Periodo inicio = Periodo.parse(integradorEsperada.getPeriodo(),"yyyyMM");
        Periodo fin = Periodo.parse(integradorEsperada.getPeriodo(),"yyyyMM");
        
        esperadaServicio.borrar(afiliado, inicio.toString(), fin.toString(), integradorEsperada.getDniUsuario());

        proceso(integradorEsperada, inicio, fin, afiliado);
    }
}
