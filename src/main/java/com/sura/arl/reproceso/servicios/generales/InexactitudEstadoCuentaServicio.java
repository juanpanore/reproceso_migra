package com.sura.arl.reproceso.servicios.generales;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.modelo.EstadoPago;
import com.sura.arl.reproceso.modelo.ResultadoInexactitud;
import com.sura.arl.reproceso.modelo.ResultadoRecalculo;
import com.sura.arl.reproceso.util.UtilObjetos;
import com.sura.arl.reproceso.util.VariablesEntorno;

@Service
public class InexactitudEstadoCuentaServicio {
    private static final Logger LOG = LoggerFactory.getLogger(InexactitudEstadoCuentaServicio.class);
    private final Double PORCENTAJE_INEXACTITUD = 0.03D; // TODO: parametro
    // TODO: parametrizar procentaje de inexactitud

    @Autowired
    private VariablesEntorno varEntorno;

    @Autowired
    public InexactitudEstadoCuentaServicio(VariablesEntorno varEntorno) {
        super();
        this.varEntorno = varEntorno;

    }

    public ResultadoInexactitud validarResultadoReproceso(EstadoCuenta original, ResultadoRecalculo reprocesada,
            Afiliado afiliado) {

        ResultadoInexactitud resultadoInexactitud = new ResultadoInexactitud();
        List<ErrorProceso> errores = new ArrayList<>();

        Double cotizacionReprocesada = reprocesada.getCotizacion().doubleValue();
        Double cotizacionReportada = reprocesada.getCotizacionReportada();
        Double diasReprocesados = reprocesada.getDias();
        Double diasReportados = reprocesada.getDiasReportados();
        Double tasaReprocesada = original.getTasa();
        Double tasaReportada = reprocesada.getTasaReportada();
        boolean tienePago = reprocesada.tienePago();

        /*
         * LOG.debug(
         * "Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{}, cotRepr:{}, cotRepo:{}, diasRepr:{}, diasRepo:{}, tasaRepo:{}, tasaRepr:{}, tienePago:{}"
         * , afiliado.getDni(), afiliado.getCobertura().getPoliza(),"
         * afiliado.getTipoAfiliado(), afiliado.getTipoCotizante(),
         * cotizacionReprocesada, cotizacionReportada, diasReprocesados, diasReportados,
         * tasaReprocesada, tasaReportada, tienePago);
         */

        // Si tiene un 97% de coincidencia del valor reportado, devuelve como afiliado
        // ok, adicionalmente setea el valor de la esperada
        if (tasaReprocesada.equals(tasaReportada) && diasReportados > 0) {

//          if(diasReprocesados.equals(diasReportados)) {
//          resultadoInexactitud.setEstadoPago(EstadoPago.AFILIADO_OK);
//          resultadoInexactitud.setSaldo(0D);
//          return resultadoInexactitud;
//      }else {
            Double porcentajeDiferencia = UtilObjetos.porcentajeDiferenciaEntreNumeros(cotizacionReprocesada,
                    cotizacionReportada);

            if (porcentajeDiferencia >= 0 && porcentajeDiferencia < PORCENTAJE_INEXACTITUD) {

                /*
                 * LOG.debug(
                 * "Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{} ----> diferencia:{} menor de {}%, es OK"
                 * , afiliado.getDni(), afiliado.getCobertura().getPoliza(),
                 * afiliado.getTipoAfiliado(), afiliado.getTipoCotizante(),
                 * porcentajeInexactitud, PORCENTAJE_INEXACTITUD);
                 */

                resultadoInexactitud.setEstadoPago(EstadoPago.AFILIADO_OK);
                resultadoInexactitud.setSeActualizaEstadoCuenta(true);
                resultadoInexactitud.setSaldo(0D);
                return resultadoInexactitud;

            }

        }
//        }

        if (!tienePago) {
            resultadoInexactitud.setEstadoPago(EstadoPago.ENRIQUES);
            errores.add(error(afiliado, CatalogoErrores.ES_UN_ENRQ));
            // LOG.debug("Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{} ----> es
            // un enrq",afiliado.getDni(), afiliado.getCobertura().getPoliza(),
            // afiliado.getTipoAfiliado(), afiliado.getTipoCotizante());
        } else {

            if (!diasReprocesados.equals(diasReportados)) {
                resultadoInexactitud.setEstadoPago(EstadoPago.DIFERENCIA_DIAS_COTIZACION);
                errores.add(error(afiliado, CatalogoErrores.DIF_CALCULO_DIAS));
                // LOG.debug("Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{} ----> dif
                // dias",afiliado.getDni(), afiliado.getCobertura().getPoliza(),
                // afiliado.getTipoAfiliado(), afiliado.getTipoCotizante());
            }

            if (!cotizacionReprocesada.equals(cotizacionReportada)) {
                resultadoInexactitud.setEstadoPago(EstadoPago.DIFERENCIA_DIAS_COTIZACION);
                errores.add(error(afiliado, CatalogoErrores.DIF_CALCULO_COT));
                // LOG.debug("Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{} ----> dif
                // cot",afiliado.getDni(), afiliado.getCobertura().getPoliza(),
                // afiliado.getTipoAfiliado(), afiliado.getTipoCotizante());

            }

            if (!tasaReprocesada.equals(tasaReportada)) {

                if (diasReprocesados.equals(diasReportados) && cotizacionReprocesada.equals(cotizacionReportada)
                        && diasReportados == 0 && cotizacionReportada == 0) {
                    resultadoInexactitud.setEstadoPago(EstadoPago.AFILIADO_OK);
                } else {
                    resultadoInexactitud.setEstadoPago(EstadoPago.DIFERENCIA_TASA);
                    errores.add(error(afiliado, CatalogoErrores.AMU));
                    // LOG.debug("Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{} ----> dif
                    // tasa",afiliado.getDni(), afiliado.getCobertura().getPoliza(),
                    // afiliado.getTipoAfiliado(), afiliado.getTipoCotizante());
                }
            }
        }

        Double diferencia = cotizacionReprocesada - cotizacionReportada;
        resultadoInexactitud.setSaldo(diferencia);

        // si no hay errores es afiliado ok
        if (errores.isEmpty()) {
            resultadoInexactitud.setEstadoPago(EstadoPago.AFILIADO_OK);
            // LOG.debug("Inexactitudes dni:{}, poliza:{}, tipoAfil:{}, tipoCot:{} ----> sin
            // diferencias, es OK",afiliado.getDni(), afiliado.getCobertura().getPoliza(),
            // afiliado.getTipoAfiliado(), afiliado.getTipoCotizante());

        } else {
            resultadoInexactitud.setErrores(errores);
        }

        return resultadoInexactitud;
    }

    private ErrorProceso error(Afiliado afiliado, String error) {
        return ErrorProceso.builder().codError(error).dni(afiliado.getDni())
                .periodo(afiliado.getCobertura().getPeriodo()).periodoGeneracion(afiliado.getCobertura().getPeriodo())
                .npoliza(afiliado.getCobertura().getPoliza()).tipoCotizante(afiliado.getTipoCotizante()).build();
    }
}
