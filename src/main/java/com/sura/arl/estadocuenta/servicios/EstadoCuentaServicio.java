package com.sura.arl.estadocuenta.servicios;

import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.accesodatos.ControlEstadoCuentaDao;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao.ObjResultadoDtlle;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.DatosTasa;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.ErrorProceso.EstadoError;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.modelo.EstadoPago;
import com.sura.arl.estadocuenta.modelo.RespuestaIbc;
import com.sura.arl.estadocuenta.servicios.DiasEsperadosServicio.DiasNoEncontradoExcepcion;
import com.sura.arl.estadocuenta.servicios.IbcEsperadaServicio.IbcNoEncontradoExcepcion;
import com.sura.arl.estadocuenta.servicios.TasaEsperadaServicio.TasaNoEncontradaExcepcion;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;
import com.sura.arl.reproceso.util.Periodo;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

@Service
public class EstadoCuentaServicio {

    private static final Logger LOG = LoggerFactory
            .getLogger(EstadoCuentaServicio.class);
    private static final String TIPO_COTIZANTE_IND_MULTIPLE_COBERTURA = "59";
    private static int N30 = 30;
    private static int NRO_TRABAJADORES_IO_DEFECTO = 100;
    private static int NRO_TRABAJADORES_EC = 30;

    private TasaEsperadaServicio tasaEsperadaServicio;

    private DiasEsperadosServicio diasEsperadosServicio;

    private IbcEsperadaServicio ibcServicio;

    private final CotizacionServicio cotizacionServicio;

    private ErroresProcesoServicio erroresProcesoServicio;

    private EstadoCuentaDao estadoCuentaDao;

    private ControlEstadoCuentaDao controlEstadoCuentaDao;

    private final ReprocesoCargaServicio reprocesoCargaServicio;

    private final ExecutorService IO;
    private final ExecutorService IO_REGISTRO;

    private ActorSelection actor;

    @Autowired
    public EstadoCuentaServicio(TasaEsperadaServicio tasaEsperadaServicio, 
            DiasEsperadosServicio diasEsperadosServicio,
            IbcEsperadaServicio ibcServicio, EstadoCuentaDao estadoCuentaDao,
            ErroresProcesoServicio erroresProcesoServicio, 
            CotizacionServicio cotizacionServicio, ExecutorService executor, 
            ActorSystem sistemaActores, 
            ControlEstadoCuentaDao controlEstadoCuentaDao,
            ReprocesoCargaServicio reprocesoCargaServicio) {
        super();
        this.tasaEsperadaServicio = tasaEsperadaServicio;
        this.diasEsperadosServicio = diasEsperadosServicio;
        this.ibcServicio = ibcServicio;
        this.estadoCuentaDao = estadoCuentaDao;
        this.erroresProcesoServicio = erroresProcesoServicio;
        this.controlEstadoCuentaDao = controlEstadoCuentaDao;
        this.cotizacionServicio = cotizacionServicio;
        this.reprocesoCargaServicio = reprocesoCargaServicio;
        this.IO = Executors.newFixedThreadPool(Integer.getInteger(
                "pool.io.conestadocuenta", NRO_TRABAJADORES_IO_DEFECTO));
        this.IO_REGISTRO = Executors.newFixedThreadPool(Integer
                .getInteger("pool.io.regestadocuenta", NRO_TRABAJADORES_EC));

        this.actor = sistemaActores.actorSelection("/user/estadoCuentaActor");

    }

    public EstadoCuenta calcularEstadoCuenta(Afiliado afiliado) {

        try {
            LOG.info(
                    "poliza {}, dni afiliado {}, periodo {},  numero coberturas {} ",
                    afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                    afiliado.getCobertura().getPeriodo(),
                    afiliado.getNmroCoberturas());
            if (afiliado.getNmroCoberturas() > 1) {
                return calcularMultipleCoberturas(afiliado);
            } else {
                return calcular(afiliado);
            }
        } catch (Exception e) {
            LOG.error("Error al calcular estado cuenta ", e);
            gestionErroresEstadoCuenta(e, afiliado);
            return null;
        }
    }

    private EstadoCuenta calcular(Afiliado afiliado)
            throws InterruptedException, ExecutionException {

        // asincronicamente se calcula la tasa
        CompletableFuture<DatosTasa> tasaFuturo = tasaEsperadaServicio
                .calcularTasaEsperada(afiliado, IO);

        // asincronicamente se calcula los dias
        CompletableFuture<Integer> diasFuturo = diasEsperadosServicio
                .calcularDiasEsperados(afiliado, IO);

        CompletableFuture<EstadoCuenta> estadoCuentaFuturo = tasaFuturo
                .thenCombine(diasFuturo, (datosTasa, dias) -> {

                    // se calcula el ibc con los dias
                    RespuestaIbc respuestaIbc = ibcServicio
                            .calcularIbc(afiliado, (Integer) dias);

                    // si se encontro salario en novedades, se remplaza el que
                    // se obtuvo en cpr
                    if (respuestaIbc.getSalario().isPresent()) {
                        afiliado.setSalario(
                                respuestaIbc.getSalario().get().intValue());
                    }

                    // se aplican reglas para calculo de cotizacion
                    Double cotizacion = cotizacionServicio.calcularCotizacion(
                            datosTasa.getTasaCalculada(),
                            respuestaIbc.getIbc().doubleValue(),
                            afiliado.getCondicion().getPeriodoCotizacion());

                    return EstadoCuenta.builder().afiliado(afiliado)
                            .tasa(datosTasa.getTasaCalculada()).dias(dias)
                            .ibc(respuestaIbc.getIbc().longValue())
                            .cotizacion(cotizacion.longValue())
                            .centroTrabajo(datosTasa.getCentroTrabajo())
                            .centroTrabajoPagador(
                                    datosTasa.getCentroTrabajoPagador())
                            .observaciones(
                                    respuestaIbc.getObservaciones().get())
                            .numeroCoberturas(afiliado.getNmroCoberturas())
                            .estadoPago(EstadoPago.MORA_PRESUNTA)
                            .saldo(cotizacion).existePago("N").build();
                });

        return estadoCuentaFuturo.get();
    }

    // Calcula el estado de cuenta para afiliados con multiples cobertura
    /*
     * public EstadoCuenta calcularEstadoCuenta(List<Afiliado>
     * grupoCoberturasPorAfiliado) { try { return
     * calcularMultipleCoberturas(grupoCoberturasPorAfiliado); } catch
     * (Exception e) { LOG.error("Error al calcular estado cuenta ", e);
     * gestionErroresEstadoCuenta(e, grupoCoberturasPorAfiliado.get(0)); return
     * null; } }
     */

    private EstadoCuenta calcularMultipleCoberturas(Afiliado afiliado)
            throws InterruptedException, ExecutionException {

        String tipoCotizanteGral = afiliado.getTipoCotizante();
        afiliado.setTipoCotizante(afiliado.getCsvTiposCotizantes());

        List<Afiliado> grupoCoberturasPorAfiliado = Stream
                .of(afiliado.getCsvTiposCotizantes().split(",")).map(str -> {
                    Afiliado afiliadoTipoCotizante = afiliado
                            .copiarAfiliadoLlave();
                    afiliadoTipoCotizante.setTipoCotizante(str);
                    return afiliadoTipoCotizante;
                }).collect(Collectors.toList());

        return diasEsperadosServicio.calcularDiasEsperados(afiliado, IO)
                .thenApply(dias -> {

                    int numeroDias = dias > N30 ? N30 : dias;

                    afiliado.setTipoCotizante(tipoCotizanteGral);

                    DatosTasa datosTasaMaxima = grupoCoberturasPorAfiliado
                            .stream()
                            .map(registro -> tasaEsperadaServicio
                                    .calcularTasaEsperada(registro, IO))
                            .collect(toList()).stream()
                            .map(CompletableFuture::join)
                            .collect(maxBy(Comparator
                                    .comparing(DatosTasa::getTasaCalculada)))
                            .orElse(new DatosTasa());

                    LOG.info("datos tasa seleccionada {}, dni {}, poliza {} ",
                            datosTasaMaxima.getTasaCalculada(),
                            afiliado.getDni(),
                            afiliado.getCobertura().getPoliza());

                    RespuestaIbc respuestaIbcMaximo = ibcServicio
                            .calcularIbc(afiliado, numeroDias);

                    afiliado.setSubtipoCotizante("00");

                    // se aplican reglas para calculo de cotizacion
                    Double cotizacion = cotizacionServicio.calcularCotizacion(
                            datosTasaMaxima.getTasaCalculada(),
                            respuestaIbcMaximo.getIbc().doubleValue(),
                            afiliado.getCondicion().getPeriodoCotizacion());

                    return EstadoCuenta.builder().afiliado(afiliado)
                            .tasa(datosTasaMaxima.getTasaCalculada())
                            .dias(numeroDias)
                            .ibc(respuestaIbcMaximo.getIbc().longValue())
                            .cotizacion(cotizacion.longValue())
                            .centroTrabajo(datosTasaMaxima.getCentroTrabajo())
                            .centroTrabajoPagador(
                                    datosTasaMaxima.getCentroTrabajoPagador())
                            .observaciones(
                                    respuestaIbcMaximo.getObservaciones().get())
                            .numeroCoberturas(afiliado.getNmroCoberturas())
                            .estadoPago(EstadoPago.MORA_PRESUNTA)
                            .saldo(cotizacion).existePago("N").build();

                }).get();
    }

    @Transactional
    public void registrar(EstadoCuenta esperada) {
        try {
            estadoCuentaDao.registrar(esperada);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Transactional
    public void actualizar(EstadoCuenta esperada) {
        try {
            estadoCuentaDao.actualizar(esperada);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Transactional
    public void merge(EstadoCuenta esperada) {
        try {
            estadoCuentaDao.merge(esperada);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public List<Cobertura> consultarCoberturas(Date fechaProceso) {
        return estadoCuentaDao.consultarCoberturasXPeriodo(fechaProceso,
                new Integer(estadoCuentaDao.getVarEntorno()
                        .getValor("dias.despues.pago")));
    }

    public void consultarPagoDeAfiliadoXCobertura(Long numFormulario,
            Afiliado afiliado) {
        estadoCuentaDao.consultarPagoDeAfiliadoXCobertura(numFormulario,
                afiliado);
    }

    public void consultarPagoDeAfiliadosXCobertura(Cobertura cobertura,
            Date fechaProceso) {
        estadoCuentaDao.consultarPagoDeAfiliadosXCobertura(cobertura,
                fechaProceso);
    }

    public List<Afiliado> obtenerSumatoriaIbcPorIndependiente(String periodo,
            String cadenaPolizas, Integer nsmmlv) {
        return estadoCuentaDao.obtenerSumatoriaIbcPorIndependiente(periodo,
                cadenaPolizas, nsmmlv);
    }

    public void marcarPagoDeAfiliados(Cobertura cobertura, Date fechaProceso) {
        estadoCuentaDao.marcarPagoDeAfiliados(cobertura, fechaProceso);
    }

    public List<EstadoCuenta> consultarEstadosCuentaXAfiliado(
            Afiliado afiliado) {
        return estadoCuentaDao.consultarEstadosCuentaXAfiliado(afiliado);
    }

    public void marcarEstadosCuentaComoPagados(Afiliado afiliado) {

        LOG.info(
                "Se solicita a Actor Estado Cuenta marcar como pagado estados cuenta : -> {}  ",
                afiliado.getDni());

        actor.tell(afiliado, ActorRef.noSender());
    }

    public void marcarPagoDeAfiliado(Long numFormulario, Afiliado afiliado) {
        estadoCuentaDao.marcarPagoDeAfiliado(numFormulario, afiliado);
    }

    public void marcarPagoIndependiente(List<Afiliado> afiliados) {
        estadoCuentaDao.marcarPagoIndependiente(afiliados);
    }

    public LocalDate getUltimoPeriodoGenerado(Afiliado afiliado) {
        return getUltimoPeriodoGenerado(afiliado, null);
    }

    public LocalDate getUltimoPeriodoGenerado(Afiliado afiliado,
            LocalDate fechaBase) {

        try {
            String ultimoPeriodo = estadoCuentaDao
                    .getUltimoPeriodo(afiliado.getCobertura().getPoliza(),
                            afiliado.getDni(), afiliado.getTipoCotizante(),
                            fechaBase != null
                                    ? Date.from(fechaBase.plusMonths(1L)
                                            .atStartOfDay(
                                                    ZoneId.systemDefault())
                                            .toInstant())
                                    : null);

            return LocalDate.from(DateTimeFormatter.ofPattern("yyyyMMdd")
                    .parse(ultimoPeriodo + "01"));
        } catch (EmptyResultDataAccessException e) {
            LOG.error("No se encontro final inicial: {}", e.getMessage());
        }
        return null;
    }

    @Transactional
    public void borrar(Afiliado afiliado, String periodoInicial,
            String periodoFinal, String usuario) {

        LOG.debug("Borrar: p:{} dni:{} ta:{} tc:{}, pi:{}, pf:{}, u:{}",
                afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                afiliado.getTipoAfiliado(), afiliado.getTipoCotizante(),
                periodoInicial, periodoFinal, usuario);

        // HU TMRARL-1430
        if (periodoInicial.compareToIgnoreCase(periodoFinal) == 0) {
            borrarEnriquesPagosIndependientes(afiliado, periodoInicial,
                    afiliado.getCobertura().getPoliza());
        } else {
            Periodo.getPeridos(Periodo.parse(periodoInicial, "yyyyMM"),
                    Periodo.parse(periodoFinal, "yyyyMM")).forEach(p -> {
                        borrarEnriquesPagosIndependientes(afiliado,
                                p.toString(),
                                afiliado.getCobertura().getPoliza());
                    });
        }

        // Realizar backup
        estadoCuentaDao.backupEstadoCuenta(afiliado.getCobertura().getPoliza(),
                afiliado.getDni(), afiliado.getTipoCotizante(), periodoInicial,
                periodoFinal, usuario);
        estadoCuentaDao.backupTrazaEstadoCuenta(
                afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                afiliado.getTipoCotizante(), periodoInicial, periodoFinal,
                usuario);

        estadoCuentaDao.borrarTrazaEstadoCuenta(
                afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                afiliado.getTipoCotizante(), periodoInicial, periodoFinal);

        estadoCuentaDao.borrarEstadoCuenta(afiliado.getCobertura().getPoliza(),
                afiliado.getDni(), afiliado.getTipoCotizante(),
                afiliado.getTipoAfiliado(), periodoInicial, periodoFinal);

        estadoCuentaDao.borrarDetalle(afiliado.getCobertura().getPoliza(),
                afiliado.getDni(), afiliado.getTipoAfiliado(), periodoInicial,
                periodoFinal);

        if (periodoInicial.compareToIgnoreCase(periodoFinal) == 0) {
            estadoCuentaDao.borrarControl(afiliado.getCobertura().getPoliza(),
                    periodoInicial);
        } else {
            Periodo.getPeridos(Periodo.parse(periodoInicial, "yyyyMM"),
                    Periodo.parse(periodoFinal, "yyyyMM")).forEach(p -> {
                        estadoCuentaDao.borrarControl(
                                afiliado.getCobertura().getPoliza(),
                                p.toString());
                    });
        }

    }

    public LocalDate getPrimerPeriodoGenerado(Afiliado afiliado,
            LocalDate date) {
        try {
            String primerPeriodo = estadoCuentaDao.getPrimerPeriodo(
                    afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                    afiliado.getTipoCotizante(), Date.from(date.minusMonths(1L)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant()));

            return LocalDate.from(DateTimeFormatter.ofPattern("yyyyMMdd")
                    .parse(primerPeriodo + "01"));
        } catch (EmptyResultDataAccessException e) {
            LOG.error("No se encontro periodo inicial: {}", e.getMessage());
        }
        return null;
    }

    public List<String> getPeriodosCobertura(Afiliado afiliado,
            Date fechaInicial, Date fechaFinal) {
        return estadoCuentaDao.getPeriodosCobertura(
                afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                afiliado.getTipoCotizante(), afiliado.getTipoAfiliado(),
                fechaInicial, fechaFinal);
    }

    private void gestionErroresEstadoCuenta(Throwable e, Afiliado afiliado) {

        ErrorProceso error = ErrorProceso.builder().dni(afiliado.getDni())
                .periodo(afiliado.getCondicion().getPeriodoCotizacion())
                .periodoGeneracion(
                        afiliado.getCobertura().getPeriodoGeneracion())
                .npoliza(afiliado.getCobertura().getPoliza())
                .tipoGeneracion(afiliado.getCondicion().getTipoGeneracion())
                .tipoCotizante(afiliado.getTipoCotizante())
                .estadoError(EstadoError.POR_CORREGIR).build();

        Throwable causa = e.getCause();
        if (causa instanceof TasaNoEncontradaExcepcion) {
            error.setCodError(CatalogoErrores.TASA_NO_ENCONTRADA);
        } else if (causa instanceof DiasNoEncontradoExcepcion) {
            error.setCodError(CatalogoErrores.DIAS_NO_ENCONTRADO);
        } else if (causa instanceof IbcNoEncontradoExcepcion) {
            error.setCodError(CatalogoErrores.IBC_NO_ENCONTRADO);
        } else {
            error.setCodError(CatalogoErrores.ERROR_NO_CONTROLADO);
            LOG.error(String.format("Error sistema en afiliado %s %s",
                    afiliado.getCobertura().getPoliza(), afiliado.getDni()), e);
        }

        erroresProcesoServicio.registrarErrorProceso(error);
    }

    @Transactional
    private void borrarEnriquesPagosIndependientes(Afiliado afiliado,
            String periodo, String poliza) {
        // si es independiente
        if ("02".equals(afiliado.getTipoAfiliado())) {
            List<ObjResultadoDtlle> estados = estadoCuentaDao
                    .obtenerEstadosCuentaAfectadasPagoIndependiente(afiliado,
                    poliza, periodo);
            LOG.info(
                    "Se encontraron {} ec marcados con pagos afiliado: {}, periodo:{}, poliza:{} , como independientes para eliminar",
                    estados.size(), afiliado.getDni(), periodo, poliza);
            if (estados.size() > 0) {
                int[] c = estadoCuentaDao
                        .borrarDetalleEstadosCuentaAfectadasPagoIndependiente(estados);
                LOG.info(
                        "--->Detalles eliminados afiliado: {}, como independientes para eliminar EstadosCuenta:{}, resultado:{}",
                        afiliado.getDni(), Arrays.toString(estados.toArray()), Arrays.toString(c));
                int[] x = estadoCuentaDao.desmarcarPagosECAfectadasPagoIndependiente(estados);
                LOG.info(
                        "--->estado cuenta afiliado: {}, como independientes para eliminar EstadosCuenta:{}, resultado:{}",
                        afiliado.getDni(), Arrays.toString(estados.toArray()), Arrays.toString(x));

                estados.forEach(r -> {
                    LOG.info("--->enviando al consolidador poliza {}, periodo:{}", r.getPoliza(), r.getPeriodo());
                    reprocesoCargaServicio.enviarMensajeConsolidador(r.getPoliza(), r.getPeriodo(), Optional.empty());
                });
            }
        }

    }
}
