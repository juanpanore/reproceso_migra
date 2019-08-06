package com.sura.arl.estadocuenta.actores;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

import com.sura.arl.afiliados.accesodatos.AfiliadosCoberturaDao;
import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Legalizacion;
import com.sura.arl.afiliados.modelo.Legalizacion.TipoProceso;
import com.sura.arl.estadocuenta.accesodatos.DetalleEstadoCuentaDao;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.modelo.SolicitudEstadoCuenta;
import com.sura.arl.estadocuenta.servicios.DiasEsperadosServicio;
import com.sura.arl.estadocuenta.servicios.ErroresProcesoServicio;
import com.sura.arl.reproceso.accesodatos.ControlNovedadesDao;
import com.sura.arl.reproceso.modelo.ResultadoInexactitud;
import com.sura.arl.reproceso.modelo.ResultadoRecalculo;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;
import com.sura.arl.reproceso.servicios.generales.InexactitudEstadoCuentaServicio;
import com.sura.arl.reproceso.util.RedondeosUtil;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class EstadoCuentaActor extends AbstractActor {

    protected final LoggingAdapter LOG = Logging.getLogger(context().system(), this);

    static final Integer N01 = 1;
    static final Integer N0 = 0;
    static final Integer N30 = 30;
    static final Integer N100 = 100;

    private AfiliadosCoberturaDao afiliadosCoberturaDao;
    private EstadoCuentaDao estadoCuentaDao;
    private DetalleEstadoCuentaDao detalleEstadoCuentaDao;

    private ControlNovedadesDao controlNovedadesDao;

    private DiasEsperadosServicio diasEsperadosServicio;
    private ErroresProcesoServicio erroresProcesoServicio;
    private final InexactitudEstadoCuentaServicio identificarInexactitudServicio;
    private final ReprocesoCargaServicio reprocesoCargaServicio;

    public EstadoCuentaActor(ApplicationContext context) {
        this.afiliadosCoberturaDao = context.getBean(AfiliadosCoberturaDao.class);
        this.controlNovedadesDao = context.getBean(ControlNovedadesDao.class);
        this.diasEsperadosServicio = context.getBean(DiasEsperadosServicio.class);
        this.estadoCuentaDao = context.getBean(EstadoCuentaDao.class);
        this.detalleEstadoCuentaDao = context.getBean(DetalleEstadoCuentaDao.class);
        this.erroresProcesoServicio = context.getBean(ErroresProcesoServicio.class);
        this.identificarInexactitudServicio = context.getBean(InexactitudEstadoCuentaServicio.class);
        this.reprocesoCargaServicio = context.getBean(ReprocesoCargaServicio.class);

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(EstadoCuenta.class, this::procesarEstadoCuenta)
                .match(SolicitudEstadoCuenta.class, this::recibirSolicitudEstadoCuenta)
                .match(Afiliado.class, this::marcarEstadosCuentaComoPagados)
                .build();
    }

    public void marcarEstadosCuentaComoPagados(Afiliado afiliado) {

        LOG.info("*************INICIA MARCACION PAGOS****************");

        try {

            if (Objects.isNull(afiliado.getLegalizacion().getTipoProceso())) {

                List<Legalizacion> pagos = controlNovedadesDao.consultarOtrosPagos(afiliado);

                if (!pagos.isEmpty()) {
                    Legalizacion pago = pagos.stream().findFirst().get();
                    afiliado.setLegalizacion(pago);
                }
            }

            CompletableFuture<List<EstadoCuenta>> futuroEstadosCuentaAfiliado = CompletableFuture
                    .supplyAsync(() -> estadoCuentaDao.consultarEstadosCuentaXAfiliado(afiliado));
            futuroEstadosCuentaAfiliado.whenComplete((respuestaEstadosCuentaAfiliado, er) -> {

                respuestaEstadosCuentaAfiliado.forEach(ec -> LOG.info(
                        "Se inicia actualizacion estados de cuenta por pago en otro contrato del afiliado {} con poliza {}, periodo {}, pagado:{}",
                        ec.getAfiliado().getDni(), ec.getAfiliado().getCobertura().getPoliza(),
                        ec.getAfiliado().getPeriodoCotizacion(), ec.getAfiliado().getTienePago()));

                Optional<EstadoCuenta> estadoCuentaPagado = respuestaEstadosCuentaAfiliado.stream()
                        .filter(estado -> estado.getExistePago().equals("S")).findFirst();

                List<EstadoCuenta> estadoCuentaParaMarcar = respuestaEstadosCuentaAfiliado.stream()
                        .filter(estado -> estado.getExistePago().equals("N")).collect(Collectors.toList());

                LOG.info("El afiliado {} en periodo {} tiene {} EC, no pagados:{}", afiliado.getDni(),
                        afiliado.getPeriodoCotizacion(), respuestaEstadosCuentaAfiliado.size(),
                        estadoCuentaParaMarcar.size());

                if (!estadoCuentaParaMarcar.isEmpty()) {

                    LOG.info("Marcando como pagado para afiliado:{}, periodo: {}---> {} EC para marcar",
                            estadoCuentaParaMarcar.get(0).getAfiliado().getDni(),
                            estadoCuentaParaMarcar.get(0).getAfiliado().getPeriodoCotizacion(),
                            estadoCuentaParaMarcar.size());

                    EstadoCuenta pagado = estadoCuentaPagado.get();

                    estadoCuentaParaMarcar.forEach(estado -> {

                        if (estadoCuentaPagado.isPresent()) {
                            LOG.info(
                                    "Se encuentra poliza con pago: {} para afiliado:{}, periodo: {}, consecutivoEC: {}",
                                    pagado.getAfiliado().getCobertura().getPoliza(), pagado.getAfiliado().getDni(),
                                    pagado.getAfiliado().getPeriodoCotizacion(),
                                    estadoCuentaPagado.get().getConsecutivo());

                            if (!Objects.isNull(afiliado.getLegalizacion().getTipoProceso())) {

                                if (afiliado.getLegalizacion().getTipoProceso().equals(TipoProceso.I)
                                /*
                                 * || afiliado.getLegalizacion().getTipoPlanilla().equals(TipoPlanilla.I) ||
                                 * afiliado.getLegalizacion().getTipoPlanilla().equals(TipoPlanilla.Y) ||
                                 * afiliado.getLegalizacion().getTipoPlanilla().equals(TipoPlanilla.M) ||
                                 * afiliado.getLegalizacion().getTipoPlanilla().equals(TipoPlanilla.A)
                                 */
                                ) {
                                    LOG.info(
                                            "Se marca EC de poliza {} y periodo {} con pago en EC con poliza {} y periodo {}",
                                            estado.getAfiliado().getCobertura().getPoliza(),
                                            estado.getAfiliado().getCobertura().getPeriodoAnioMes(),
                                            pagado.getAfiliado().getCobertura().getPoliza(),
                                            pagado.getAfiliado().getCobertura().getPeriodoAnioMes());

                                    // Recalcular Valores- Validar si debe ir otra vez
                                    Double proporcianalIBC = proporcionalIBC(pagado.getIbcReportado(),
                                            pagado.getDiasReportados(), estado.getDias());

                                    ResultadoRecalculo recalculo = new ResultadoRecalculo();
                                    recalculo.setDias(Double.valueOf(estado.getDias()));
                                    recalculo.setDiasReportados(Double.valueOf(pagado.getDiasReportados()));
                                    recalculo
                                            .setCotizacion(Double.valueOf((proporcianalIBC * estado.getTasa()) / N100));
                                    recalculo.setCotizacionReportada(Double.valueOf((pagado.getCotizacionReportada())));
                                    recalculo.setIbc(proporcianalIBC);
                                    recalculo.setIbcReportado(Double.valueOf(pagado.getIbcReportado()));
                                    recalculo.setTasaReportada(Double.valueOf(pagado.getTasaReportada()));
                                    recalculo.setTienePago(true);

                                    ResultadoInexactitud resultadoInexactitud = identificarInexactitudServicio
                                            .validarResultadoReproceso(estado, recalculo, estado.getAfiliado());

                                    EstadoCuenta.Builder estadoCuenta = EstadoCuenta.builder()
                                            .ibc(RedondeosUtil
                                                    .redondearIbc(recalculo.getIbc(),
                                                            estado.getAfiliado().getCobertura().getPeriodoAnioMes())
                                                    .longValue())
                                            .cotizacion(RedondeosUtil
                                                    .redondearCotizacion(recalculo.getCotizacion(),
                                                            estado.getAfiliado().getCobertura().getPeriodoAnioMes())
                                                    .longValue())
                                            .cotizacionReportada(pagado.getCotizacionReportada().longValue())
                                            .diasReportados(pagado.getDiasReportados().intValue())
                                            .ibcReportado(pagado.getIbcReportado().longValue())
                                            .tasaReportada(pagado.getTasaReportada()).afiliado(afiliado)
                                            .existePago(pagado.getExistePago())
                                            .estadoPago(resultadoInexactitud.getEstadoPago())
                                            .saldo(resultadoInexactitud.getSaldo());
                                    estadoCuentaDao.marcarEstadosCuentaComoPagados(estado.getAfiliado(),
                                            estadoCuenta.build(), afiliado.getLegalizacion().getNumeroFormulario());
                                    int detallesCreados = detalleEstadoCuentaDao.registrarConConsecutivo(
                                            estado.getAfiliado().getCobertura().getPoliza(), estado.getConsecutivo(),
                                            pagado.getConsecutivo());

                                    if (detallesCreados == 0) {
                                        // lanza excepcion si no se registraron detalles
                                        throw new AccesoDatosExcepcion("Error no se registraron detalles, cobertura:"
                                                + estado.getAfiliado().getCobertura().getPoliza() + ", consecutivoEC:"
                                                + estado.getConsecutivo() + ", consecutivoECPagado:"
                                                + pagado.getConsecutivo());
                                    }
                                    LOG.info(
                                            "Creando detalle EC : -> poliza:{} , afiliado:{} , periodo:{} ,consecutivoEC:{}",
                                            estado.getAfiliado().getCobertura().getPoliza(),
                                            estado.getAfiliado().getDni(),
                                            estado.getAfiliado().getCobertura().getPeriodoAnioMes(),
                                            estado.getConsecutivo());

                                    LOG.info("Se actualiza estado cuenta : -> {} , {} , {} ",
                                            estado.getAfiliado().getCobertura().getPoliza(),
                                            estado.getAfiliado().getDni(),
                                            estado.getAfiliado().getCobertura().getPeriodoAnioMes());

                                    reprocesoCargaServicio.enviarMensajeConsolidador(estado.getAfiliado(),
                                            Optional.of(afiliado.getLegalizacion().getNumeroFormulario()));

                                    LOG.info("Se envia a cola reproceso completado : -> {} , {} , {} ",
                                            estado.getAfiliado().getCobertura().getPoliza(),
                                            estado.getAfiliado().getDni(),
                                            estado.getAfiliado().getCobertura().getPeriodoAnioMes());

                                } else {
                                    LOG.info("No cumple condiciones para marcar EC: -> {}  ",
                                            estado.getAfiliado().getCobertura().getPoliza());
                                }
                            } else {
                                LOG.info("Integrador: no se encontro otro pago del mismo tipo : -> {}  ",
                                        estado.getAfiliado().getDni(), afiliado.getLegalizacion().getTipoPlanilla());
                            }
                        }
                    });

                }
            });

        } catch (Exception e) {

            ErrorProceso error = ErrorProceso.builder().dni(afiliado.getDni())
                    .periodo(afiliado.getCondicion().getPeriodoCotizacion())
                    .periodoGeneracion(afiliado.getCobertura().getPeriodoGeneracion())
                    .npoliza(afiliado.getCobertura().getPoliza())
                    .tipoGeneracion(afiliado.getCondicion().getTipoGeneracion())
                    .tipoCotizante(afiliado.getTipoCotizante()).estadoError(ErrorProceso.EstadoError.POR_CORREGIR)
                    .build();

            error.setCodError(CatalogoErrores.ERROR_NO_CONTROLADO);
            LOG.error(String.format("Error la procesar afiliado %s", afiliado.getDni()), e);

            erroresProcesoServicio.registrar(error);
        }

    }

    private Double proporcionalIBC(double ibcReportado, int diasLaborados, int diasEsperadas) {

        if (diasLaborados == N0) {
            return ibcReportado;
        }

        return (((ibcReportado / diasLaborados) * N30) / N30) * diasEsperadas;
    }

    public void recibirSolicitudEstadoCuenta(SolicitudEstadoCuenta solicitudEstadoCuenta) {

        Cobertura cobertura = new Cobertura();
        cobertura.setPoliza(solicitudEstadoCuenta.getPoliza());
        cobertura.setPeriodoGeneracion(solicitudEstadoCuenta.getPeriodoGeneracion());
        cobertura.setPeriodo(solicitudEstadoCuenta.getPeriodoCotizacion());

        // consultar afiliado para enviar a actor que calcula el estado de
        // cuenta
        Afiliado afiliado = afiliadosCoberturaDao.consultarAfiliado(cobertura, solicitudEstadoCuenta.getDniAfiliado(),
                solicitudEstadoCuenta.getCertificadoAfiliado());

        // Enviar solicitud de calculo de estado de cuenta
        ActorSelection cotizacionActorRef = context().actorSelection("/user/cotizacionActor");
        cotizacionActorRef.tell(afiliado, self());

        sender().tell("RECIBIDO", self());
    }

    public void procesarEstadoCuenta(EstadoCuenta esperada) {

        try {
            LOG.info("Procesando Estado de Cuenta");

//            Activar update cuando sea necesario.
//            if (diasEsperadosServicio.consultarMultipleCobertura(esperada.getAfiliado()) > N01) {
//                //update
//                LOG.info("Procesando Update Estado de Cuenta");
//                estadoCuentaDao.actualizar(esperada);
//            } else {
//                //insert
            LOG.info("Procesando Insert Estado de Cuenta");
            estadoCuentaDao.registrar(esperada);
//            }
            sender().tell("RECIBIDO", self());
        } catch (Exception e) {
            Afiliado afiliado = esperada.getAfiliado();
            ErrorProceso error = ErrorProceso.builder().dni(afiliado.getDni())
                    .periodo(afiliado.getCondicion().getPeriodoCotizacion())
                    .periodoGeneracion(afiliado.getCobertura().getPeriodoGeneracion())
                    .npoliza(afiliado.getCobertura().getPoliza())
                    .tipoGeneracion(afiliado.getCondicion().getTipoGeneracion())
                    .tipoCotizante(afiliado.getTipoCotizante()).estadoError(ErrorProceso.EstadoError.POR_CORREGIR)
                    .build();

            error.setCodError(CatalogoErrores.ERROR_NO_CONTROLADO);
            LOG.error(String.format("Error sistema en afiliado %s", afiliado.getDni()), e);

            erroresProcesoServicio.registrar(error);
        }
    }

}
