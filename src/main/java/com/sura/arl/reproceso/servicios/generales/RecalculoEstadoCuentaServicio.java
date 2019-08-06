package com.sura.arl.reproceso.servicios.generales;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.accesodatos.AfiliadosCoberturaDao;
import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.DiasEsperadosDao;
import com.sura.arl.estadocuenta.accesodatos.IbcCotizacionDao;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.reproceso.accesodatos.ReprocesoEstadoCuentaDao;
import com.sura.arl.reproceso.modelo.ConsolidadoNovedades;
import com.sura.arl.reproceso.modelo.DatosNovedades;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.ResultadoRecalculo;
import com.sura.arl.reproceso.modelo.TipoPlanilla;
import com.sura.arl.reproceso.modelo.excepciones.ForzarReprocesoExcepcion;
import com.sura.arl.reproceso.modelo.excepciones.ReprocesoAfiliadoCanceladoExcepcion;
import com.sura.arl.reproceso.util.RedondeosUtil;
import com.sura.arl.reproceso.util.UtilCache;
import com.sura.arl.reproceso.util.UtilReproceso;

@Service
public class RecalculoEstadoCuentaServicio {

    private final ReprocesoEstadoCuentaDao reprocesoEstadoCuentaDao;
    private final IbcCotizacionDao ibcCotizacionDao;
    private final ActualizacionCentroTrabajoServicio actualizacionCTServicio;

    private static final Logger LOG = LoggerFactory.getLogger(RecalculoEstadoCuentaServicio.class);

    static final Integer N100 = 100;
    static final Integer N0 = 0;
    static final Integer N1 = 1;
    static final Integer N1000 = 1000;
    static final Double D30 = 30D;
    static final Integer N30 = 30;
    static final Double D0 = 0D;
    static final String LEY2388 = "2388";
    static final String LEY1747 = "1747";
    static final Long FUENTELEGALIZACION = 89L;
    static final String PLANILLA_N_MODIFICAR = "M";
    static final String PERIODO_REGLA = "201702";
    static final String ID_CACHE_SALMIN = "SAL_MIN_";

    @Autowired
    public RecalculoEstadoCuentaServicio(ReprocesoEstadoCuentaDao reprocesoEstadoCuentaDao,
            IbcCotizacionDao ibcCotizacionDao, DiasEsperadosDao diasEsperadosDao,
            ActualizacionCentroTrabajoServicio actualizacionCTServicio, EstadoCuentaServicio estadoCuentaServicio,
            AfiliadosCoberturaDao afiliadosCoberturaDao) {
        super();
        this.reprocesoEstadoCuentaDao = reprocesoEstadoCuentaDao;
        this.ibcCotizacionDao = ibcCotizacionDao;
        this.actualizacionCTServicio = actualizacionCTServicio;
    }

    public ResultadoRecalculo calcularValores(EstadoCuenta estadoCuentaActual, Afiliado afiliado, String periodo,
            Optional<Long> numeroFormulario, Optional<InfoNovedadVCT> infoVCT)
            throws ReprocesoAfiliadoCanceladoExcepcion {

        String periodoYYYYMM = periodo.substring(2, 6).concat(periodo.substring(N0, 2));
        String dni = afiliado.getDni();
        String poliza = afiliado.getCobertura().getPoliza();
        ResultadoRecalculo resultado = new ResultadoRecalculo();

        // -------------------------------------------------------
        ConsolidadoNovedades consolidado = reprocesoEstadoCuentaDao.consultarNovedadesAfiliado(afiliado, periodoYYYYMM);
        List<DatosNovedades> novedadesAusentismo = consolidado.getAusentismo();
        List<DatosNovedades> novedadesLaboradas = consolidado.getLaboradas();

        // --19/08/2019
        // si por alguna razon, la esperada tiene pago y tiene llega por lo menos 1
        // novedad,
        // entoncs se debe volver a generar la esperada especifica y volver a reprocesar
        if ((novedadesAusentismo.size() > 0 || novedadesLaboradas.size() > 0)
                && "S".equals(estadoCuentaActual.getExistePago())) {
                LOG.info("Se detiene reproceso, ya tiene reproceso realizado, se envia a integrador para volver a generar -> dni:{}, poliza:{}, periodo:{}, tipoAfil:{}, tipoCot:{}, formulario:{}",
                dni, poliza, periodo, afiliado.getTipoAfiliado(), afiliado.getTipoCotizante(),
                numeroFormulario.orElse(-1L));
            throw new ForzarReprocesoExcepcion(dni, periodo, poliza);
        }

        estadoCuentaActual = actualizacionCTServicio.procesarCambioCT(estadoCuentaActual, afiliado, periodoYYYYMM,
                consolidado.getListaVct());
        // -------------------------------------------------------

        // si por alguna razon no devuelve formularios, es q no encontro pagos validos,
        // por ejemplo estudiante que paga con planilla no valida
        if (consolidado.getFormulariosAfectados().isEmpty()) {
            return reportarSinPago(estadoCuentaActual, afiliado, numeroFormulario);
        }

        Double sumatoriaIbcConAusentismo = D0;
        Double sumatoriaDiasConAusentismo = D0;
        Double sumatoriaIbcLaborados = D0;
        Double sumatoriaDiasLaborados = D0;
        Double sumatoriaCotizacionLaborados = D0;
        boolean tieneIngreso = false;
        boolean tieneRetiro = false;
        boolean tieneNovedadesLaboradas = false;
        boolean tieneNovedadesAusentismo = false;
        Double tasaEsperada = estadoCuentaActual.getTasa();
        Double diasEsperados = estadoCuentaActual.getDias().doubleValue();
        Double ibcEsperado = estadoCuentaActual.getIbc().doubleValue();
        Double cotizacionEsperada = estadoCuentaActual.getCotizacion().doubleValue();
        int totalNovedadesAusentismo = 0;
        Double tasaMayorReportada = 0D;
        Double tasaPlanilla$ = 0D;

        // Reproceso 2388
        if (LEY2388.equals(consolidado.getLey())) {
            totalNovedadesAusentismo = novedadesAusentismo.size();
            tieneNovedadesLaboradas = novedadesLaboradas.size() > 0;
            tieneNovedadesAusentismo = totalNovedadesAusentismo > 0;

            // recorre las novedades laboradas
            for (DatosNovedades n : novedadesLaboradas) {
                sumatoriaIbcLaborados += n.getIbc();
                sumatoriaDiasLaborados += n.getDias();
                sumatoriaCotizacionLaborados += n.getCotizacion();
                tieneIngreso = tieneIngreso ? true : n.isIngreso();
                tieneRetiro = tieneRetiro ? true : n.isRetiro();
                if (n.getTasa() > tasaMayorReportada && !TipoPlanilla.$.equals(n.getTipoPlanilla())) {
                    tasaMayorReportada = n.getTasa();
                }

                // si llega reportada una planilla $, se hace una sumatoria de la tasa, para
                // despues sumarla
                if (TipoPlanilla.$.equals(n.getTipoPlanilla())) {
                    tasaPlanilla$ += n.getTasa();
                }
            }

            // recorre las novedades con ausentismo
            for (DatosNovedades n : novedadesAusentismo) {
                sumatoriaIbcConAusentismo += n.getIbc();
                sumatoriaDiasConAusentismo += n.getDias();
                tieneIngreso = tieneIngreso ? true : n.isIngreso();
                tieneRetiro = tieneRetiro ? true : n.isRetiro();
                if (n.getTasa() > tasaMayorReportada && !TipoPlanilla.$.equals(n.getTipoPlanilla())) {
                    tasaMayorReportada = n.getTasa();
                }

                // si llega reportada una planilla $, se hace una sumatoria de la tasa, para
                // despues sumarla
                if (TipoPlanilla.$.equals(n.getTipoPlanilla())) {
                    tasaPlanilla$ += n.getTasa();
                }
            }

            // si tiene mas de 30 dias de ausentismo, le asigna 30 dias
            if (sumatoriaDiasConAusentismo > 30) {
                sumatoriaDiasConAusentismo = D30;
            }

            // los dias de ausentismo son 30 y no tiene laboradas
            if (tieneNovedadesAusentismo && !tieneNovedadesLaboradas) {
                resultado.setDias(0D);
                resultado.setIbc(0D);
                resultado.setCotizacion(0D);

                // los dias de ausentismo son 30 y tiene dias laborados
            } else if (tieneNovedadesAusentismo && tieneNovedadesLaboradas) {

                if (sumatoriaDiasLaborados > 30) {
                    resultado.setDias(30D);
                    resultado.setIbc(validarTopesIbc(afiliado, (sumatoriaIbcLaborados / sumatoriaDiasLaborados) * 30,
                            resultado.getDias(), periodoYYYYMM));

                } else {
                    if (sumatoriaDiasLaborados > diasEsperados) {
                        resultado.setDias(diasEsperados);
                        resultado.setIbc(validarTopesIbc(afiliado,
                                (sumatoriaIbcLaborados * diasEsperados) / sumatoriaDiasLaborados, resultado.getDias(),
                                periodoYYYYMM));
                    } else {
                        resultado.setDias(sumatoriaDiasLaborados);
                        resultado.setIbc(
                                validarTopesIbc(afiliado, sumatoriaIbcLaborados, resultado.getDias(), periodoYYYYMM));
                    }
                }
                resultado.setCotizacion((resultado.getIbc() * tasaEsperada) / N100);

            } else if (!tieneNovedadesAusentismo && tieneNovedadesLaboradas) {

                resultado.setDias(diasEsperados);

                // TMRARL-1448
                if (afiliado.esIndependiente() && sumatoriaIbcLaborados > N30) {
                    resultado.setIbc(validarTopesIbc(afiliado, (sumatoriaIbcLaborados * diasEsperados) / N30,
                            resultado.getDias(), periodoYYYYMM));
                } else {
                    resultado.setIbc(
                            validarTopesIbc(afiliado, (sumatoriaIbcLaborados * diasEsperados) / sumatoriaDiasLaborados,
                                    resultado.getDias(), periodoYYYYMM));
                }
                resultado.setCotizacion((resultado.getIbc() * tasaEsperada) / N100);

            } else {
                resultado.setDias(diasEsperados);
                resultado.setIbc(validarTopesIbc(afiliado,
                        (sumatoriaIbcLaborados / (sumatoriaDiasLaborados < 1 ? 1 : sumatoriaDiasLaborados))
                                * diasEsperados,
                        resultado.getDias(), periodoYYYYMM));
                resultado.setCotizacion((resultado.getIbc() * tasaEsperada) / N100);
            }

            resultado.setPagos(UtilReproceso.convertirNovedadesADetalles(afiliado, consolidado.getLaboradasOriginal(),
                    consolidado.getAusentismoOriginal(), estadoCuentaActual.getConsecutivo()));

            // Reproceso 1747
        } else {

            tieneNovedadesLaboradas = novedadesLaboradas.size() > 0;
            tieneNovedadesAusentismo = novedadesAusentismo.size() > 0;
            boolean entroPorN = false;
            Double diasAusentismo1747 = 0D;
            Double diasLaborados1747 = 0D;
            Double ibcLaborado1747 = 0D;

            // recorre las novedades sin ausentismo
            for (DatosNovedades n : novedadesLaboradas) {
                sumatoriaIbcLaborados += n.getIbc();
                sumatoriaDiasLaborados += n.getDias();
                sumatoriaCotizacionLaborados += n.getCotizacion();

                tieneIngreso = tieneIngreso ? true : n.isIngreso();
                tieneRetiro = tieneRetiro ? true : n.isRetiro();

                if (n.getTasa() > tasaMayorReportada) {
                    tasaMayorReportada = n.getTasa();
                }

                if (LEY1747.equals(n.getLey())) {
                    diasAusentismo1747 += D30 - n.getDias();
                    diasLaborados1747 += n.getDias();
                    ibcLaborado1747 += n.getIbc();
                }
            }

            // recorre las novedades sin ausentismo
            for (DatosNovedades n : novedadesAusentismo) {

                if (TipoPlanilla.N.equals(n.getTipoPlanilla())) {

                    sumatoriaDiasConAusentismo += n.getDias();
                    sumatoriaIbcConAusentismo += n.getIbc();
                    sumatoriaCotizacionLaborados += n.getCotizacion();
                    entroPorN = true;
                }

                if (n.getTasa() > tasaMayorReportada) {
                    tasaMayorReportada = n.getTasa();
                }
            }

            // dias de ausentismo = 30 - laborados
            if (!entroPorN) {
                sumatoriaDiasConAusentismo = diasAusentismo1747;
            } else {
                sumatoriaDiasLaborados = sumatoriaDiasLaborados + sumatoriaDiasConAusentismo
                        - (diasLaborados1747 + sumatoriaDiasConAusentismo);
                sumatoriaDiasConAusentismo = diasLaborados1747 + sumatoriaDiasConAusentismo;

                sumatoriaIbcLaborados = sumatoriaIbcLaborados + sumatoriaIbcConAusentismo
                        - (ibcLaborado1747 + sumatoriaIbcConAusentismo);
                sumatoriaIbcConAusentismo = ibcLaborado1747 + sumatoriaIbcConAusentismo;

            }

            // si tiene mas de 30 dias ausentismo, le asigna 30 dias
            if (sumatoriaDiasConAusentismo > 30) {
                sumatoriaDiasConAusentismo = D30;
            }

            // los dias de ausentismo son 30 y no tiene laboradas
            if (tieneNovedadesAusentismo && sumatoriaDiasLaborados == 0) {

                resultado.setDias(0D);
                resultado.setIbc(0D);
                resultado.setCotizacion(0D);

                // los dias de ausentismo son 30 y tiene dias laborados
            } else if (tieneNovedadesAusentismo && sumatoriaDiasLaborados > 0) {
                if (sumatoriaDiasLaborados > diasEsperados) {
                    resultado.setDias(diasEsperados);
                    resultado.setIbc((sumatoriaIbcLaborados * diasEsperados) / sumatoriaDiasLaborados);
                } else {
                    resultado.setDias(sumatoriaDiasLaborados);
                    resultado.setIbc(sumatoriaIbcLaborados);

                }
                resultado.setCotizacion(resultado.getIbc() * tasaEsperada / 100);

            } else {
                resultado.setDias(diasEsperados);
                resultado.setIbc((sumatoriaIbcLaborados / (sumatoriaDiasLaborados < 1 ? 1 : sumatoriaDiasLaborados))
                        * diasEsperados);
                resultado.setCotizacion((resultado.getIbc() * tasaEsperada) / N100);
            }

            resultado.setPagos(UtilReproceso.convertirNovedadesADetalles(afiliado, novedadesLaboradas,
                    novedadesAusentismo, estadoCuentaActual.getConsecutivo()));
        }

        // si tiene valor la planilla $, se suma a la mayor tasa reportada
        if (tasaPlanilla$ != 0) {
            LOG.debug("Tiene pagos en planilla $, se le suma {}, a la tasa mayor reportada {}", tasaPlanilla$,
                    tasaMayorReportada);

            tasaMayorReportada = BigDecimal.valueOf(tasaMayorReportada).add(BigDecimal.valueOf(tasaPlanilla$))
                    .doubleValue();
            // Double.sum(tasaMayorReportada,tasaPlanilla$);
        }

        // redondea segun reglas
        resultado.setIbc(RedondeosUtil.redondearIbc(resultado.getIbc(), periodoYYYYMM));

        resultado.setCotizacion(RedondeosUtil.redondearCotizacion(resultado.getCotizacion(), periodoYYYYMM));
        // setea valores reportados
        resultado.setCotizacionReportada(sumatoriaCotizacionLaborados);
        resultado.setDiasReportados(sumatoriaDiasLaborados);
        resultado.setIbcReportado(sumatoriaIbcLaborados);
        resultado.setTasaReportada(tasaMayorReportada);
        resultado.setTienePago(true);

        LOG.debug(
                "Se reprocesa dni:{}, poliza:{}, periodo:{}, tipoAfil:{}, tipoCot:{}, formulario:{}, es2388?:{} -> resultado ->"
                        + " ibcOri:{}, ibcRep:{}, ibcEsp:{}, diasOri:{}, diasRep:{}, diasEsp:{},"
                        + " tasaOri:{}, tasaRep:{}, cotOri:{}, cotRep:{}, cotEsp:{},  diasLabo:{},"
                        + " diasAus:{}, ibcLabo:{}, ibcAus:{}, cotLabo:{}, ing?:{}, ret?:{}",
                dni, poliza, periodo, afiliado.getTipoAfiliado(), afiliado.getTipoCotizante(),
                numeroFormulario.orElse(-1L), consolidado.getLey(), ibcEsperado, resultado.getIbcReportado(),
                resultado.getIbc(), diasEsperados, resultado.getDiasReportados(), resultado.getDias(), tasaEsperada,
                resultado.getTasaReportada(), cotizacionEsperada, resultado.getCotizacionReportada(),
                resultado.getCotizacion(), sumatoriaDiasLaborados, sumatoriaDiasConAusentismo, sumatoriaIbcLaborados,
                sumatoriaIbcConAusentismo, sumatoriaCotizacionLaborados, tieneIngreso, tieneRetiro);
        return resultado;

    }

    private ResultadoRecalculo reportarSinPago(EstadoCuenta estadoCuentaActual, Afiliado afiliado,
            Optional<Long> numeroFormulario) {
        ResultadoRecalculo resultado = new ResultadoRecalculo();

        String dni = afiliado.getDni();
        String poliza = afiliado.getCobertura().getPoliza();

        Double diasEsperados = estadoCuentaActual.getDias().doubleValue();
        Double ibcEsperado = estadoCuentaActual.getIbc().doubleValue();
        Double cotizacionEsperada = estadoCuentaActual.getCotizacion().doubleValue();
        Double tasaEsperada = estadoCuentaActual.getTasa();

        resultado.setDias(diasEsperados);
        resultado.setIbc(ibcEsperado);
        resultado.setCotizacion(cotizacionEsperada);
        resultado.setCotizacionReportada(0D);
        resultado.setDiasReportados(0D);
        resultado.setIbcReportado(0D);
        resultado.setTasaReportada(0D);
        resultado.setTienePago(false);
        resultado.setPagos(new ArrayList<>());

        LOG.debug(
                "Se reprocesa dni:{}, poliza:{}, tipoAfil:{}, formulario:{} -> resultado -> ibcOri:{}, diasOri:{}, tasaOri:{}, cotOri:{} ... no se encontro pago valido reportado",
                dni, poliza, afiliado.getTipoAfiliado(), numeroFormulario.orElse(-1L), ibcEsperado, diasEsperados,
                tasaEsperada, cotizacionEsperada);

        return resultado;
    }

    private Double validarTopesIbc(Afiliado afiliado, Double ibc, Double dias, String periodo) {

        // no sigue la validacion, si el valor es 0
        if (ibc == 0) {
            return ibc;
        }

        Double salarioMinimoPeriodo = 0D;
        if (!Objects.isNull(UtilCache.obtener(ID_CACHE_SALMIN + periodo))) {
            salarioMinimoPeriodo = (Double) UtilCache.obtener(ID_CACHE_SALMIN + periodo);
        } else {
            salarioMinimoPeriodo = ibcCotizacionDao.consultarSalarioMinimoXperiodo(periodo);
            UtilCache.agregar(ID_CACHE_SALMIN + periodo, salarioMinimoPeriodo);
        }

        Double topeMinIBC = afiliado.getCondicion().getIbcMinimo() * salarioMinimoPeriodo;
        Double topeMaxIBC = afiliado.getCondicion().getIbcMaximo() * salarioMinimoPeriodo;

        Double ibcMinimoProporcional = (topeMinIBC / N30) * dias;

        // se corrige el ibc segun topes
        // si el top min ibc es mayor al calculado, se corrige el calculado
        if (ibcMinimoProporcional.compareTo(ibc) > N0) {
            ibc = (topeMinIBC / N30) * dias;
        }

        // si el tope max ibc es menor al calculado, se corrige el calculado
        if (topeMaxIBC.compareTo(ibc) < N0) {
            ibc = (topeMaxIBC / N30) * dias;

            /*
             * }else if(topeMaxIBC.compareTo(ibc) > N0){ ibc = topeMaxIBC;
             */
        }

        return ibc;
    }

}
