package com.sura.arl.reproceso.servicios.generales;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.DetalleEstadoCuentaDao;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaMetadataDao;
import com.sura.arl.estadocuenta.accesodatos.TrazaEstadoCuentaDao;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.modelo.EstadoPago;
import com.sura.arl.estadocuenta.servicios.ErroresProcesoServicio;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.integrador.accesodatos.CambiosEstadoCuentaDao;
import com.sura.arl.integrador.modelo.Registro;
import com.sura.arl.reproceso.accesodatos.NovedadesDao;
import com.sura.arl.reproceso.modelo.CampoActualizado;
import com.sura.arl.reproceso.modelo.CampoActualizado.Campo;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.ResultadoInexactitud;
import com.sura.arl.reproceso.modelo.ResultadoRecalculo;
import com.sura.arl.reproceso.modelo.excepciones.CambiosEsperadosExcepcion;
import com.sura.arl.reproceso.modelo.excepciones.ForzarReprocesoExcepcion;
import com.sura.arl.reproceso.modelo.excepciones.ReprocesoAfiliadoCanceladoExcepcion;
import com.sura.arl.reproceso.servicios.RenesServicio;
import com.sura.arl.reproceso.servicios.UltimosValoresCotizadosServicio;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.actor.ActorSystem;

@Service
public class ReprocesoAfiliadoServicio {

    private final EstadoCuentaDao estadoCuentaDao;
    private final InexactitudEstadoCuentaServicio identificarInexactitudServicio;
    private final RecalculoEstadoCuentaServicio recalculoEsperadaServicio;
    private final TrazaEstadoCuentaDao trazaEstadoCuentaDao;
    private final UltimosValoresCotizadosServicio ultimosValoresCotizadosServicio;
    private final NovedadesDao novedadesDao;
    private final RenesServicio renesServicio;
    private final ErroresProcesoServicio erroresProcesoServicio;
    private final DetalleEstadoCuentaDao detalleEstadoCuentaDao;
    private final EstadoCuentaServicio estadoCuentaServicio;
    private final CambiosEstadoCuentaDao cambiosEstadoCuentaDao;

    private static final Logger LOG = LoggerFactory.getLogger(ReprocesoAfiliadoServicio.class);
    static final String S = "S";

    @Autowired
    private VariablesEntorno varEntorno;

    @Autowired
    public ReprocesoAfiliadoServicio(EstadoCuentaDao estadoCuentaDao,
            RecalculoEstadoCuentaServicio recalculoEsperadaServicio, TrazaEstadoCuentaDao trazaEstadoCuentaDao,
            UltimosValoresCotizadosServicio ultimosValoresCotizadosServicio, NovedadesDao novedadesDao,
            RenesServicio renesServicio, ErroresProcesoServicio erroresProcesoServicio,
            EstadoCuentaMetadataDao estadoCuentaMetadataDao, DetalleEstadoCuentaDao detalleEstadoCuentaDao,
            InexactitudEstadoCuentaServicio identificarInexactitudServicio, EstadoCuentaServicio estadoCuentaServicio,
            CambiosEstadoCuentaDao cambiosEstadoCuentaDao, ActorSystem sistemaActores) {
        super();

        this.estadoCuentaDao = estadoCuentaDao;
        this.recalculoEsperadaServicio = recalculoEsperadaServicio;
        this.trazaEstadoCuentaDao = trazaEstadoCuentaDao;
        this.ultimosValoresCotizadosServicio = ultimosValoresCotizadosServicio;
        this.novedadesDao = novedadesDao;
        this.renesServicio = renesServicio;
        this.erroresProcesoServicio = erroresProcesoServicio;
        this.identificarInexactitudServicio = identificarInexactitudServicio;
        this.detalleEstadoCuentaDao = detalleEstadoCuentaDao;
        this.estadoCuentaServicio = estadoCuentaServicio;
        this.cambiosEstadoCuentaDao = cambiosEstadoCuentaDao;
    }

    @Transactional(rollbackFor = ReprocesoAfiliadoCanceladoExcepcion.class)
    public ResultadoRecalculo ejecutarRecalculo(Afiliado afiliado) throws ReprocesoAfiliadoCanceladoExcepcion {

        List<ErrorProceso> consolidadoErrores = new ArrayList<>();
        ResultadoRecalculo resultadoRecalculo = null;
        Double tasaOriginal = null;
        String ctOriginal = null;
        String ctpOriginal = null;
        EstadoCuenta esperadaActual = null;
        Optional<Long> numFormulario = Optional.ofNullable(afiliado.getLegalizacion().getNumeroFormulario());
        String periodo = afiliado.getCobertura().getPeriodo();
        String periodoYYYYMM = afiliado.getCobertura().getPeriodoAnioMes();
        Optional<InfoNovedadVCT> infoVCT = Optional.empty();

        // inicia con recalculo
        try {
            // procesa los errores encontrados
            if (afiliado.getTipoError() != null) {
                throw new ReprocesoAfiliadoCanceladoExcepcion(afiliado.getTipoError());
            }

            LOG.info("dni afiliado {} , periodo {}, formulario {}", afiliado.getDni(), periodoYYYYMM,
                    numFormulario.orElse(-1L));

            // lo primero es buscar el estado de cuenta
            esperadaActual = estadoCuentaDao.consultarXafiliadoPeriodo(afiliado, periodoYYYYMM);

            // LOG.info("Existe esperada actual {} ", esperadaActual);

            // Se ejecuta primero el registro de renes, dado que si no
            // existe esperada no debe continuar el proceso.
            // Genera RENES (si aplica)
            if (!Objects.isNull(esperadaActual)) {

                // estas var solo se usan si hubo cambio de CT, para reportar en la traza
                ctpOriginal = esperadaActual.getCentroTrabajoPagador();
                ctOriginal = esperadaActual.getCentroTrabajo();
                tasaOriginal = esperadaActual.getTasa();

                // TODO: temporal para evitar q se reprocesen las q ya esten reprocesadas
                /*
                 * if (!EstadoPago.MORA_PRESUNTA.equals(esperadaActual.getEstadoPago()) &&
                 * !EstadoPago.ENRIQUES.equals(esperadaActual.getEstadoPago())) { LOG.debug(
                 * "No se reprocesa (ya esta reprocesado) ---> afiliado:{}, periodo:{}, tipoCotizante:{}, poliza:{} "
                 * , afiliado.getDni(), afiliado.getPeriodoCotizacion(),
                 * afiliado.getTipoCotizante(), afiliado.getCobertura().getPoliza()); return
                 * null; }
                 */

//                if (numFormulario.isPresent()) {
                int renesEliminados = renesServicio.borrar(afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                        periodoYYYYMM, afiliado.getTipoAfiliado(), numFormulario.orElse(null));

                if (renesEliminados > 0) {

                    LOG.debug(
                            "Rene borrado para afiliado:{}, periodo:{}, tipoCotizante:{}, poliza:{}, numeroFormulario:{}",
                            afiliado.getDni(), afiliado.getPeriodoCotizacion(), afiliado.getTipoCotizante(),
                            afiliado.getCobertura().getPoliza(), numFormulario.orElse(null));
//                    }
                } else {
                    LOG.debug(
                            "No elimino renes para afiliado:{}, periodo:{}, tipoCotizante:{}, poliza:{}, numeroFormulario:{}",
                            afiliado.getDni(), afiliado.getPeriodoCotizacion(), afiliado.getTipoCotizante(),
                            afiliado.getCobertura().getPoliza(), numFormulario.orElse(null));
                }

            } else {

                if (numFormulario.isPresent()) {

                    // TODO Es necesaria esta validacion? al intentar insertar el rene igual busca
                    // que no este ya en los renes.. verificar
                    if (renesServicio.existe(afiliado.getCobertura().getPoliza(), afiliado.getDni(), periodoYYYYMM,
                            afiliado.getTipoAfiliado(), numFormulario.get())) {
                        LOG.debug("Tiene un rene para afiliado:{}, periodo:{}, tipAfiliado:{}, poliza:{} ",
                                afiliado.getDni(), periodoYYYYMM, afiliado.getTipoAfiliado(),
                                afiliado.getCobertura().getPoliza());
                        return null;
                    }

                    // intenta registrar el rene: busca los datos en legalizacion,
                    // tambn q no tenga un rene, tambn q no este en el estado de cuenta
                    Integer renesRegistrados = renesServicio.registrar(numFormulario.get(), afiliado.getDni(),
                            afiliado.getTipoCotizante(), afiliado.getTipoAfiliado());

                    if (renesRegistrados > 0) {
                        LOG.debug("Es un rene para afiliado:{}, periodo:{}, tipoCotizante:{}, poliza:{} ",
                                afiliado.getDni(), periodoYYYYMM, afiliado.getTipoCotizante(),
                                afiliado.getCobertura().getPoliza());
                    } else {
                        // TODO verificar si aparece esto en log, no registro el rene posiblemente x el
                        // tipo de cotizante
                        LOG.debug(
                                "*Es un rene, pero no queda registrado para afiliado:{}, periodo:{}, tipoCotizante:{}, poliza:{} ",
                                afiliado.getDni(), periodoYYYYMM, afiliado.getTipoCotizante(),
                                afiliado.getCobertura().getPoliza());
                    }

                    // temporal para tener metrica de este error al probar
                    consolidadoErrores.add(error(afiliado, CatalogoErrores.ES_UN_RENE));
                    erroresProcesoServicio.registrar(consolidadoErrores);

                    return null;
                }
            }

            // recalcula los valores esperados
            resultadoRecalculo = recalculoEsperadaServicio.calcularValores(esperadaActual, afiliado, periodo,
                    numFormulario, infoVCT);

            // siempre se actualizan los valores reportados en el estado de
            // cuenta
            EstadoCuenta.Builder esperadaActualizada = EstadoCuenta.builder()
                    .centroTrabajo(esperadaActual.getCentroTrabajo())
                    .centroTrabajoPagador(esperadaActual.getCentroTrabajoPagador())
                    .cotizacionReportada(resultadoRecalculo.getCotizacionReportada().longValue())
                    .diasReportados(resultadoRecalculo.getDiasReportados().intValue())
                    .ibcReportado(resultadoRecalculo.getIbcReportado().longValue())
                    .tasaReportada(resultadoRecalculo.getTasaReportada()).afiliado(afiliado)
                    .tasa(esperadaActual.getTasa()).dias(resultadoRecalculo.getDias().intValue())
                    .cotizacion(resultadoRecalculo.getCotizacion().longValue())
                    .existePago(resultadoRecalculo.tienePago() ? "S" : "N")
                    .ibc(resultadoRecalculo.getIbc().longValue());

            // si hay alguna diferencia con la esperada actual,
            // realiza la actualizacion
            ResultadoInexactitud resultadoInexactitud = identificarInexactitudServicio
                    .validarResultadoReproceso(esperadaActual, resultadoRecalculo, afiliado);

            EstadoPago estadoPago = resultadoInexactitud.getEstadoPago();
            esperadaActualizada.estadoPago(estadoPago);
            esperadaActualizada.saldo(resultadoInexactitud.getSaldo());

            // si la validacion devuelve alguna diferencia, se reporta en la traza
            // si el afiliado tiene error ó si es afiliado ok y la validacion de inexactitud
            // tiene seActualizaEstadoCuenta=true, quiere decir q la diferencia entre
            // valores
            // reportados y esperados es del 97%, actualiza el valor del estado
            // de cuenta
            if (!EstadoPago.AFILIADO_OK.equals(estadoPago)
                    || EstadoPago.AFILIADO_OK.equals(estadoPago) && resultadoInexactitud.seActualizaEstadoCuenta()) {

                esperadaActualizada.cotizacion(resultadoRecalculo.getCotizacion().longValue())
                        .dias(resultadoRecalculo.getDias().intValue()).ibc(resultadoRecalculo.getIbc().longValue())
                        .tasa(esperadaActual.getTasa()).saldo(resultadoInexactitud.getSaldo());

                if (resultadoInexactitud.seActualizaEstadoCuenta()) {
                    esperadaActualizada.cotizacion(resultadoRecalculo.getCotizacionReportada().longValue())
                            .dias(resultadoRecalculo.getDiasReportados().intValue())
                            .ibc(resultadoRecalculo.getIbcReportado().longValue()).tasa(esperadaActual.getTasa())
                            .saldo(resultadoInexactitud.getSaldo());
                }

                // solo reporta errores si no es afiliado_ok
                if (!EstadoPago.AFILIADO_OK.equals(estadoPago)) {
                    // agrega los errores generados en la validacion de inexactitud
                    consolidadoErrores.addAll(resultadoInexactitud.getErrores());
                }
            }

            try {
            	 LOG.info("Se inicia actualizacion ec");
                estadoCuentaDao.actualizar(esperadaActualizada.build());
                LOG.info("Se inicia ingreso dtlle");
                detalleEstadoCuentaDao.registrar(resultadoRecalculo.getPagos());
                registrarTrazaCambio(esperadaActual, esperadaActualizada.build(), afiliado, ctOriginal, ctpOriginal,
                        tasaOriginal);
                novedadesDao.actualizarProcesado(afiliado, true, resultadoRecalculo.getPagos());

            } catch (CambiosEsperadosExcepcion e) {
                LOG.debug(
                        "Sin valores para cambiar en el reproceso, afiliado:{}, periodo:{}, tipoCotizante:{}, poliza:{} ",
                        afiliado.getDni(), afiliado.getPeriodoCotizacion(), afiliado.getTipoCotizante(),
                        afiliado.getCobertura().getPoliza());
            }

            // Se actualiza el ultimo ibc, salario y periodo cotizado
           // ultimosValoresCotizadosServicio.actualizar(afiliado, periodoYYYYMM);

            // si llega a esta excepcion, ocurrio algun error controlado
        } catch (ForzarReprocesoExcepcion fr) {
            LOG.info("No se reprocesa afiliado:{}, poliza:{}, periodo:{}, tipCot:{}, formulario:{}, motivo: {}",
                    afiliado.getDni(), afiliado.getCobertura().getPoliza(), afiliado.getCobertura().getPeriodo(),
                    afiliado.getTipoCotizante(), numFormulario,
                    "Se envia al integrador para borrar, crear y reprocesar estado de cuenta nuevamente");

            registrarEnIntegrador(afiliado.getDni(), afiliado.getCobertura().getPoliza(),
                    afiliado.getCobertura().getPeriodoAnioMes(), afiliado.getTipoCotizante(),
                    afiliado.getTipoAfiliado(), numFormulario.orElse(null));

            throw fr;

        } catch (ReprocesoAfiliadoCanceladoExcepcion rc) {
            consolidadoErrores.add(error(afiliado, rc.getMessage()));
            erroresProcesoServicio.registrar(consolidadoErrores);

            LOG.info("No se reprocesa afiliado:{}, poliza:{}, periodo:{}, tipCot:{}, formulario:{}, motivo:{}",
                    afiliado.getDni(), afiliado.getCobertura().getPoliza(), afiliado.getCobertura().getPeriodo(),
                    afiliado.getTipoCotizante(), numFormulario, rc.getMessage());
            throw rc;

            // error no controlado
        } catch (Exception e) {
            consolidadoErrores.add(error(afiliado, CatalogoErrores.ERROR_NO_CONTROLADO));
            erroresProcesoServicio.registrar(consolidadoErrores);
            LOG.error(
                    "Error no controlado, no se pudo reprocesar afiliado:{}, poliza:{}, periodo:{}, tipCot:{}, formulario:{}, error:{} ",
                    afiliado.getDni(), afiliado.getCobertura().getPoliza(), afiliado.getCobertura().getPeriodo(),
                    afiliado.getTipoCotizante(), numFormulario, e.getMessage(), e);

            throw e;
        }

        // Si se presentaron errores se debe registrar los errores.
        if (!consolidadoErrores.isEmpty()) {
            erroresProcesoServicio.registrar(consolidadoErrores);
        }

        if (afiliado.getTipoAfiliado().equals("02")) {

            estadoCuentaServicio.marcarEstadosCuentaComoPagados(afiliado);

        }

        return resultadoRecalculo;

    }

    private void registrarEnIntegrador(String dni, String poliza, String periodo, String tipoCotizante,
            String tipoAfiliado, Long numeroFormulario) {

        Long seq = cambiosEstadoCuentaDao.getProximaSecuencia();

        String params = "{id:" + seq + ", tipo:'REPROCESO_FLUJO_COMPLETO', poliza:'" + poliza + "', dniAfiliado:'" + dni
                + "', dniUsuario:'" + dni + "', periodo:'" + periodo + "', tipoAfiliado:'" + tipoAfiliado
                + "', tipoCotizante:'" + tipoCotizante + "', formularioPago:" + numeroFormulario + "}";

        Registro registro = new Registro();
        registro.setDni(dni);
        registro.setDsParametros(params);
        registro.setEstado("NUEVO");
        registro.setId(seq);
        registro.setPoliza(poliza);

        cambiosEstadoCuentaDao.ingresarRegistroATramitar(registro, dni);

    }

    private ErrorProceso error(Afiliado afiliado, String error) {
        return ErrorProceso.builder().codError(error).dni(afiliado.getDni())
                .periodo(afiliado.getCobertura().getPeriodo()).periodoGeneracion(afiliado.getCobertura().getPeriodo())
                .npoliza(afiliado.getCobertura().getPoliza()).tipoCotizante(afiliado.getTipoCotizante()).build();
    }

    private void registrarTrazaCambio(EstadoCuenta actual, EstadoCuenta nueva, Afiliado afiliado, String ctOriginal,
            String ctpOriginal, Double tasaOriginal) {

        List<CampoActualizado> listado = new ArrayList<>();
        String periodo = nueva.getAfiliado().getCobertura().getPeriodo();
        String periodoYYYYMM = periodo.substring(2, 6).concat(periodo.substring(0, 2));

        CampoActualizado.Builder c = CampoActualizado.builder().dni(nueva.getAfiliado().getDni())
                .nmperiodo(periodoYYYYMM).dniIngresa(getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA))
                .poliza(afiliado.getCobertura().getPoliza()).tipoCotizante(afiliado.getTipoCotizante())
                .tipoAfiliado(afiliado.getTipoAfiliado());

        if (!nueva.getCotizacion().equals(actual.getCotizacion())) {
            c.campo(Campo.COTIZACION_ESPERADA);
            c.valorViejo(actual.getCotizacion().toString());
            c.valorNuevo(nueva.getCotizacion().toString());
            listado.add(c.build());
        }

        if (!nueva.getDias().equals(actual.getDias())) {
            c.campo(Campo.DIAS_ESPERADOS);
            c.valorViejo(actual.getDias().toString());
            c.valorNuevo(nueva.getDias().toString());
            listado.add(c.build());
        }

        if (!nueva.getIbc().equals(actual.getIbc())) {
            c.campo(Campo.IBC_ESPERADO);
            c.valorViejo(actual.getIbc().toString());
            c.valorNuevo(nueva.getIbc().toString());
            listado.add(c.build());
        }

        if (!nueva.getCentroTrabajo().equals(ctOriginal)) {
            c.campo(Campo.CT);
            c.valorViejo(ctOriginal);
            c.valorNuevo(nueva.getCentroTrabajo().toString());
            listado.add(c.build());
        }

        if (!nueva.getCentroTrabajoPagador().equals(ctpOriginal)) {
            c.campo(Campo.CTP);
            c.valorViejo(ctpOriginal);
            c.valorNuevo(nueva.getCentroTrabajoPagador().toString());
            listado.add(c.build());
        }

        if (!nueva.getTasa().equals(tasaOriginal)) {
            c.campo(Campo.TASA_ESPERADA);
            c.valorViejo(actual.getTasa().toString());
            c.valorNuevo(nueva.getTasa().toString());
            listado.add(c.build());
        }

        if (!listado.isEmpty()) {
            trazaEstadoCuentaDao.registroComoLote(listado);
        }
    }

    public VariablesEntorno getVarEntorno() {
        return varEntorno;
    }

    public void setVarEntorno(VariablesEntorno varEntorno) {
        this.varEntorno = varEntorno;
    }
}
