package com.sura.arl.reproceso.servicios.generales;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.TasaEsperadaDao;
import com.sura.arl.estadocuenta.accesodatos.TrazaEstadoCuentaDao;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.DatosTasa;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao.DatosCentroTrabajo;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao.DatosCobertura;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao.RespuestaFechasCobertura;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoPagadorDao;
import com.sura.arl.reproceso.accesodatos.CoberturaDao;
import com.sura.arl.reproceso.accesodatos.HistoricoAfiliadosDao;
import com.sura.arl.reproceso.accesodatos.HistoricoCambioCTDao;
import com.sura.arl.reproceso.accesodatos.HistoricoInconsistenciaCambioCTDao;
import com.sura.arl.reproceso.accesodatos.HistoricoInconsistenciaCambioCTDao.InconsistenciaCambioCT;
import com.sura.arl.reproceso.accesodatos.ParametrosDao;
import com.sura.arl.reproceso.modelo.HistoricoAfiliado;
import com.sura.arl.reproceso.modelo.HistoricoCambioCT;
import com.sura.arl.reproceso.modelo.HistoricoInconsistenciaCambioCT;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.excepciones.ReprocesoAfiliadoCanceladoExcepcion;
import com.sura.arl.reproceso.util.UtilFechas;
import com.sura.arl.reproceso.util.UtilObjetos;
import com.sura.arl.reproceso.util.VariablesEntorno;

@Service
public class ActualizacionCentroTrabajoServicio {

    private static final Logger LOG = LoggerFactory.getLogger(ActualizacionCentroTrabajoServicio.class);

    private final CentroTrabajoDao centroTrabajoDao;
    private final HistoricoAfiliadosDao historicoAfiliadosDao;
    private final HistoricoCambioCTDao historicoCambioCTDao;
    private final CoberturaDao coberturaDao;
    private final ParametrosDao parametrosDao;
    private final HistoricoInconsistenciaCambioCTDao hcoInconsistenciaCambioCT;
    private final CentroTrabajoPagadorDao centroTrabajoPagadorDao;
    private final TasaEsperadaDao tasaEsperadaDao;
    private VariablesEntorno varEntorno;

    static final Date FINAL_COBERTURA = UtilFechas.obtenerFecha("31", "12", "3000");
    static final String[] tiposCotizantesEstudiantes = { "23" };

    @Autowired
    public ActualizacionCentroTrabajoServicio(CentroTrabajoDao centroTrabajoDao, VariablesEntorno varEntorno,
            TrazaEstadoCuentaDao trazaEstadoCuentaDao, HistoricoAfiliadosDao historicoAfiliadosDao,
            ParametrosDao parametrosDao, HistoricoCambioCTDao historicoCambioCTDao, CoberturaDao coberturaDao,
            HistoricoInconsistenciaCambioCTDao hcoInconsistenciaCambioCT,
            CentroTrabajoPagadorDao centroTrabajoPagadorDao, TasaEsperadaDao tasaEsperadaDao) {
        super();
        this.centroTrabajoDao = centroTrabajoDao;
        this.varEntorno = varEntorno;
        this.historicoAfiliadosDao = historicoAfiliadosDao;
        this.parametrosDao = parametrosDao;
        this.historicoCambioCTDao = historicoCambioCTDao;
        this.coberturaDao = coberturaDao;
        this.hcoInconsistenciaCambioCT = hcoInconsistenciaCambioCT;
        this.centroTrabajoPagadorDao = centroTrabajoPagadorDao;
        this.tasaEsperadaDao = tasaEsperadaDao;
    }

    public EstadoCuenta procesarCambioCT(EstadoCuenta estadoCuenta, Afiliado afiliado, String periodo,
            List<InfoNovedadVCT> listainfoVCT) throws ReprocesoAfiliadoCanceladoExcepcion {
        
        for (InfoNovedadVCT infoVct : listainfoVCT) {
            estadoCuenta = procesarCambioCT(estadoCuenta, afiliado, periodo, infoVct);
        }
        return estadoCuenta;
    }

    @SuppressWarnings("unused")
    public EstadoCuenta procesarCambioCT(EstadoCuenta estadoCuenta, Afiliado afiliado, String periodo,
            InfoNovedadVCT infoVCT) throws ReprocesoAfiliadoCanceladoExcepcion {
    return estadoCuenta;
    }
    
    
    @SuppressWarnings("unused")
    public EstadoCuenta procesarCambioCT2(EstadoCuenta estadoCuenta, Afiliado afiliado, String periodo,
            InfoNovedadVCT infoVCT) throws ReprocesoAfiliadoCanceladoExcepcion {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMM");
        LocalDate periodoActual = LocalDate.now();
        String ctReportado = infoVCT.getCentroTrabajo();
        String ctOriginal = estadoCuenta.getAfiliado().getCobertura().getSucursal();
        Double tasaOtiginal = estadoCuenta.getTasa();
        String poliza = afiliado.getCobertura().getPoliza();
        String dni = afiliado.getDni();
        String tipoCotizante = afiliado.getTipoCotizante();
        String tipoAfiliado = afiliado.getTipoAfiliado();
        String fuente = getVarEntorno().getValor("fuente.proceso");
        String dniIngresa = getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA);
        String existeCentro = "0";
        Date inicioVCT = null;
        Date finVCT = null;
        String rangoCambio = "";

        // valida se aplica el cambio de CT, si no aplica, se devueve el estado de
        // cuenta original
        if (!aplicaActualizacion(afiliado, estadoCuenta, infoVCT)) {
            LOG.debug(
                    "No aplica cambio CT, poliza:{}, periodo:{}, dni:{}, ctRep:{}, ctEsp:{}, tipCotRep:{}, tipoCotEsp:{}, tipoAfilRep:{}. tipoAfilEsp:{}",
                    poliza, periodo, dni, ctReportado, ctOriginal, tipoCotizante,
                    estadoCuenta.getAfiliado().getTipoCotizante(), tipoAfiliado,
                    estadoCuenta.getAfiliado().getTipoAfiliado());

            return estadoCuenta;
        }

        // se setea obj de inconsistencia x si hay error
        HistoricoInconsistenciaCambioCT inconsistencia = new HistoricoInconsistenciaCambioCT();
        inconsistencia.setDni(dni);
        inconsistencia.setNmperiodo(periodo);
        inconsistencia.setPoliza(poliza);
        inconsistencia.setSucursalAnterior(ctOriginal);
        inconsistencia.setSucursalNueva(ctReportado);
        inconsistencia.setDniIngresa(dniIngresa);

        // busca datos de la cobertura, dependiendo si es estudiante o no
        DatosCobertura datosCobertura = centroTrabajoDao.consultarDatosCobertura(periodo, poliza, dni, tipoCotizante,
                tipoAfiliado);

        if (Objects.isNull(datosCobertura)) {
            LOG.error(
                    "Error en cambio CT, sin datos de cobertura ,poliza:{}, periodo:{}, dni:{}, ctRep:{}, ctEsp:{}, tipCotRep:{}, tipoCotEsp:{}, tipoAfilRep:{}. tipoAfilEsp:{}",
                    poliza, periodo, dni, ctReportado, ctOriginal, tipoCotizante,
                    estadoCuenta.getAfiliado().getTipoCotizante(), tipoAfiliado,
                    estadoCuenta.getAfiliado().getTipoAfiliado());
            throw new ReprocesoAfiliadoCanceladoExcepcion(CatalogoErrores.ERROR_CAMBIO_CT);
        }

        // busca datos del CT
        DatosCentroTrabajo datosCT = centroTrabajoDao.consultarDatosCT(periodo, poliza, ctReportado);

        if (Objects.isNull(datosCT)) {
            LOG.debug("No se hace cambio CT, sin datos del CT ,poliza:{}, periodo:{}, dni:{}, ctReportado:{}", poliza,
                    periodo, dni, ctReportado);

            inconsistencia.setCambio(false);
            inconsistencia.setCodigoInconsistencia(InconsistenciaCambioCT.CT_SIN_COBERTURA_PERIODO);
            hcoInconsistenciaCambioCT.insertar(inconsistencia);

            return estadoCuenta;
        }

        // busca datos del CTP
        String nuevoCTP = centroTrabajoPagadorDao.buscarCTP(periodo, poliza, ctReportado);

        if (Objects.isNull(nuevoCTP)) {
            LOG.debug("No se hace cambio CT, sin datos para el CTP ,poliza:{}, periodo:{}, dni:{}, ctReportado:{}",
                    poliza, periodo, dni, ctReportado);

            inconsistencia.setCambio(false);
            inconsistencia.setCodigoInconsistencia(InconsistenciaCambioCT.CT_SIN_COBERTURA_PERIODO);
            hcoInconsistenciaCambioCT.insertar(inconsistencia);

            return estadoCuenta;
        }

        // busca fechas de coberturas
        RespuestaFechasCobertura fechasCobertura = centroTrabajoDao.consultarFechasCobertura(periodo, estadoCuenta,
                afiliado);
        if (fechasCobertura.getTotalCoberturas() == 0) {
            LOG.error(
                    "Error en cambio CT, sin datos del fechas de coberturas ,poliza:{}, periodo:{}, dni:{}, ctReportado:{}",
                    poliza, periodo, dni, ctReportado);
            throw new ReprocesoAfiliadoCanceladoExcepcion(CatalogoErrores.ERROR_CAMBIO_CT);
        }

        if (fechasCobertura.getTotalCoberturas() > 1) {
            LOG.debug("No se procesa VCT, tiene multiples coberturas:{}, poliza:{}, dni:{}, tipoCot:{}, tipoAfil:{} ",
                    periodo, poliza, dni, estadoCuenta.getAfiliado().getTipoAfiliado(),
                    estadoCuenta.getAfiliado().getTipoCotizante());

            inconsistencia.setCambio(false);
            inconsistencia.setCodigoInconsistencia(InconsistenciaCambioCT.AFIL_MULTIPLES_COBERTURAS_PERIODO);
            hcoInconsistenciaCambioCT.insertar(inconsistencia);

            // se devuelve el estado de cuenta sin procesar
            return estadoCuenta;
        }

        try {
            // periodo inicial del vct = 1er dia del periodo de cotizacion

            calendar.setTime(dateformat.parse(periodo));
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            inicioVCT = calendar.getTime();

            // busca el valor parametrizado
            Long periodosCambioVCT = Long.valueOf(parametrosDao.obtenerTodosParametros().get("reproceso.numero.periodos.vct"));
            LocalDate periodoParametrizado = periodoActual.withDayOfMonth(1).minusMonths(periodosCambioVCT);

            LocalDate periodoCotizadoLD = dateformat.parse(periodo).toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();

            boolean actualizarCuerpolizaRiesto = true;
            if (periodoCotizadoLD.isAfter(periodoParametrizado)
                    && FINAL_COBERTURA.equals(fechasCobertura.getMaxFebaja())) {
                finVCT = FINAL_COBERTURA;

            } else if (periodoCotizadoLD.isAfter(periodoParametrizado)
                    && !FINAL_COBERTURA.equals(fechasCobertura.getMaxFebaja())) {
                finVCT = fechasCobertura.getMaxFebaja();

            } else {
                calendar.setTime(dateformat.parse(periodo));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                finVCT = calendar.getTime();

                // se setea los dias de la febaja a 30 sin tocar el mes, para poder comparalo
                // con el finvct(que ya esta igualado a 30)
                calendar.setTime(fechasCobertura.getMaxFebaja());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date periodoFebaja = calendar.getTime();

                if (!finVCT.equals(periodoFebaja)) {
                    actualizarCuerpolizaRiesto = false;
                }
            }

            // HISTORICOS

            // busca el registro en T094_HISTORICO_AFILIADOS que haya q
            // actualizar
            HistoricoAfiliado historicoEncontrado = null;
            historicoEncontrado = historicoAfiliadosDao.consultarRegistroAfectado(poliza, dni,
                    datosCobertura.getCertificado(), periodo);
            if (Objects.isNull(historicoEncontrado)) {
                LOG.error(
                        "Error en cambio CT, sin datos en hcoAfiliados, poliza:{}, periodo:{}, dni:{}, ncertificado:{}",
                        poliza, periodo, dni, datosCobertura.getCertificado());
                throw new ReprocesoAfiliadoCanceladoExcepcion(CatalogoErrores.ERROR_CAMBIO_CT);
            }

            // actualiza el registro
            HistoricoAfiliado historicoActualizado = (HistoricoAfiliado) UtilObjetos.clonar(historicoEncontrado);

            // si el hco encontrado, tiene fealta igual al inicio del vct
            // no crea registro nuevo, y actualiza el que encuentra
            if (historicoActualizado.getFealta().equals(inicioVCT)) {

                historicoActualizado.setSucursalPagadora(nuevoCTP);
                historicoActualizado.setSucursal(datosCT.getSucursalActualizar());
                historicoActualizado.setDniModifica(dniIngresa);
                historicoActualizado.setDniIngresa(dniIngresa); // TODO si deberia?
                historicoActualizado.setFebaja(finVCT);
                historicoAfiliadosDao.actualizar(historicoActualizado, historicoEncontrado.getFebaja());

            } else {
                // actualiza el registro
                historicoActualizado.setFebaja(UtilFechas.agregarDias(inicioVCT, -1));
                historicoActualizado.setDniModifica(dniIngresa);
                historicoAfiliadosDao.actualizarFebaja(historicoActualizado, historicoEncontrado.getFebaja());

                // nuevo registro
                HistoricoAfiliado historicoNuevo = (HistoricoAfiliado) UtilObjetos.clonar(historicoEncontrado);
                historicoNuevo.setFealta(inicioVCT);
                historicoNuevo.setFebaja(finVCT);
                historicoNuevo.setSucursalPagadora(nuevoCTP);
                historicoNuevo.setSucursal(datosCT.getSucursalActualizar());
                historicoNuevo.setDniIngresa(dniIngresa);
                historicoAfiliadosDao.insertar(historicoNuevo);
            }

            if (!actualizarCuerpolizaRiesto) {
                HistoricoAfiliado historicoNuevo2 = (HistoricoAfiliado) UtilObjetos.clonar(historicoEncontrado);
                historicoNuevo2.setFealta(UtilFechas.agregarDias(finVCT, +1));
                historicoNuevo2.setFebaja(historicoEncontrado.getFebaja());
                historicoNuevo2.setDniIngresa(dniIngresa);
                historicoAfiliadosDao.insertar(historicoNuevo2);
            }

            // historico mvto CT
            HistoricoCambioCT historicoCT = new HistoricoCambioCT();
            historicoCT.setDni(dni);
            historicoCT.setPoliza(poliza);
            historicoCT.setFuente(fuente);
            historicoCT.setPeriodosCambio(obtenerPeriodosCambio(periodo, inicioVCT, finVCT));
            historicoCT.setDniIngresa(dniIngresa);
            historicoCT.setSucursalAnterior(ctOriginal);
            historicoCT.setSucursalNueva(datosCT.getSucursalActualizar());

            historicoCambioCTDao.insertar(historicoCT);

            // si aplica, actualiza cuerpoliza riesgo
            if (actualizarCuerpolizaRiesto) {
                coberturaDao.actualizarCTCuerpolizaRiesgo(poliza, dni, datosCobertura.getCertificado(),
                        datosCT.getSucursalActualizar(), datosCobertura.getFebaja());
            }

            // busca la tasa con los cambios realizados
            DatosTasa datosTasa = tasaEsperadaDao.consultarTasa(poliza, periodo, dni,
                    afiliado.getCondicion().getTipoTasa(), tipoCotizante, tipoAfiliado);

            // actualiza los valores del estado de cuenta actualizado con los valores nuevos
            EstadoCuenta estadoCuentaActualizado = EstadoCuenta.builder().afiliado(estadoCuenta.getAfiliado())
                    .centroTrabajo(datosCT.getSucursalActualizar()).centroTrabajoPagador(nuevoCTP)
                    .cotizacion(estadoCuenta.getCotizacion()).dias(estadoCuenta.getDias())
                    .estadoPago(estadoCuenta.getEstadoPago()).existePago(estadoCuenta.getExistePago())
                    .ibc(estadoCuenta.getIbc()).numeroCoberturas(estadoCuenta.getNumeroCoberturas())
                    .observaciones(estadoCuenta.getObservaciones()).saldo(estadoCuenta.getSaldo())
                    .tasa(datosTasa.getTasaCalculada()).build();

            LOG.debug(
                    "Se realiza cambio CT, poliza:{}, periodo:{}, dni:{}, ctReportado:{}, ctOriginal:{}, tasaNueva:{}, tasaOriginal:{}, ctpNuevo:{}",
                    poliza, periodo, dni, ctReportado, ctOriginal, datosTasa.getTasaCalculada(), tasaOtiginal,
                    nuevoCTP);

            return estadoCuentaActualizado;

        } catch (Exception e) {
            LOG.error(
                    "Error en cambio CT, error no controlado, poliza:{}, periodo:{}, dni:{}, ctReportado:{}, error:{}",
                    poliza, periodo, dni, ctReportado, e.getMessage(), e);
            throw new ReprocesoAfiliadoCanceladoExcepcion(CatalogoErrores.ERROR_CAMBIO_CT);
        }

    }

    private String obtenerPeriodosCambio(String periodo, Date inicioVCT, Date finVCT) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");

        String periodoInicial = df.format(inicioVCT);
        String periodoFinal = df.format(finVCT);

        if (periodoFinal.equals(periodo)) {
            return periodo;
        } else {
            return periodoInicial.concat(" - ").concat(periodoFinal);
        }
    }

    private boolean aplicaActualizacion(Afiliado af, EstadoCuenta estadoCuenta, InfoNovedadVCT infoVct) {

        // si el ct reportado no es diferente del esperado, no aplica
        if (infoVct.getCentroTrabajo().equals(estadoCuenta.getAfiliado().getCobertura().getSucursal())) {
            return false;
        }

        // si el tipoAf reportado es igual al esperado y no importa si coinciden los
        // tipoCot,
        // se aplica siempre y cuando no sea estudiante
        if (af.getTipoAfiliado().equals(estadoCuenta.getAfiliado().getTipoAfiliado())
                && !Arrays.stream(tiposCotizantesEstudiantes).anyMatch(af.getTipoCotizante()::equals)) {
            return true;
        }

        // si el tipoAf reportado es igual al esperado y tipoCot son iguales y es
        // estudiante
        if (af.getTipoAfiliado().equals(estadoCuenta.getAfiliado().getTipoAfiliado())
                && af.getTipoCotizante().equals(estadoCuenta.getAfiliado().getTipoCotizante())
                && Arrays.stream(tiposCotizantesEstudiantes).anyMatch(af.getTipoCotizante()::equals)) {

            return true;
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