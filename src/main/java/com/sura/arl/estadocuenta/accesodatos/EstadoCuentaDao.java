package com.sura.arl.estadocuenta.accesodatos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao.ObjResultadoDtlle;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.modelo.EstadoPago;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.excepciones.CambiosEsperadosExcepcion;

@Repository
public class EstadoCuentaDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(EstadoCuentaDao.class);

    @Transactional
    public void registrar(EstadoCuenta registro) {

        Map<String, Object> params = obtenerParametros(registro);

        getJdbcTemplate().update(getVarEntorno().getValor("registro.esperada"), params);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void registrar(List<EstadoCuenta> estadoCuentas) {

        if (estadoCuentas.isEmpty()) {
            return;
        }

        int i = 0;
        Map<String, Object> params[] = new Map[estadoCuentas.size()];
        for (EstadoCuenta esperada : estadoCuentas) {
            Map<String, Object> paramsEsperada = obtenerParametros(esperada);

            params[i++] = paramsEsperada;
        }

        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("registro.esperada"), params);
    }

    @Transactional
    public void borrar(String periodoCotizacion, String poliza) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodoCotizacion", periodoCotizacion);
        params.put("poliza", poliza);

        getJdbcTemplate().update(getVarEntorno().getValor("borrar.estadocuenta"), params);
    }

    private Map<String, Object> obtenerParametros(EstadoCuenta esperada) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", esperada.getAfiliado().getCobertura().getPoliza());
        params.put("dniAfiliado", esperada.getAfiliado().getDni());
        params.put("tipoCotizante", esperada.getAfiliado().getTipoCotizante());
        params.put("subTipoCotizante", esperada.getAfiliado().getSubtipoCotizante());
        params.put("tipoAfiliado", esperada.getAfiliado().getTipoAfiliado());
        params.put("dias", esperada.getDias());
        params.put("tasa", esperada.getTasa());
        params.put("ibc", esperada.getIbc());
        params.put("cotizacion", esperada.getCotizacion());
        params.put("salario", esperada.getAfiliado().getSalario());
        params.put("centroTrabajo", esperada.getCentroTrabajo());
        params.put("centroTrabajoPagador", esperada.getCentroTrabajoPagador());
        params.put("tipoGeneracion", esperada.getAfiliado().getCondicion().getTipoGeneracion());
        params.put("periodoGeneracion", esperada.getAfiliado().getCobertura().getPeriodoGeneracion());
        params.put("periodoProceso", esperada.getAfiliado().getCondicion().getPeriodoCotizacion());
        params.put("salario", esperada.getAfiliado().getSalario());
        params.put("dniIngresa", esperada.getUsuarioOperacion() == null ? getVarEntorno().getValor("usuario.dniingresa")
                : esperada.getUsuarioOperacion());
        params.put("observaciones", esperada.getObservaciones());
        params.put("numeroCoberturas", esperada.getNumeroCoberturas());
        params.put("estadoPago", esperada.getEstadoPago().getEquivalencia());
        params.put("fechaLimitePago", esperada.getAfiliado().getFechaLimitePago());
        params.put("existePago", esperada.getExistePago());
        params.put("saldo", Objects.isNull(esperada.getSaldo()) ? null : esperada.getSaldo() * -1);
        return params;
    }

    public List<Cobertura> consultarCoberturasXPeriodo(Date fechaProceso, Integer rango) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fechaProceso", fechaProceso);
        params.put("rango", rango);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.estado.cuenta.coberturas"), params,
                (ResultSet rs, int index) -> {
                    Cobertura cobertura = new Cobertura();
                    cobertura.setPoliza(rs.getString("NMPOLIZA"));
                    cobertura.setPeriodo(rs.getString("PERIODO"));
                    cobertura.setFechaLimitePago(rs.getDate("FELIMITE_PAGO"));
                    cobertura.setFechaInicioProceso(rs.getDate("FEPROCESAR_ENR"));
                    return cobertura;
                });
    }

    @Transactional
    public void consultarPagoDeAfiliadosXCobertura(Cobertura cobertura, Date fechaProceso) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", cobertura.getPoliza());
        params.put("fechaProceso", fechaProceso);
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        getJdbcTemplate().update(getVarEntorno().getValor("marcar.pago.estado.cuenta.afiliados"), params);
    }

    public void consultarPagoDeAfiliadoXCobertura(Long numFormulario, Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("numFormulario", numFormulario);
        params.put("poliza", afiliado.getCobertura().getPoliza());
        params.put("periodo", afiliado.getCobertura().getPeriodo());
        params.put("dniAfiliado", afiliado.getDni());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        getJdbcTemplate().update(getVarEntorno().getValor("marcar.pago.estado.cuenta.afiliado"), params);
    }

    // TODO esto se usa?? solo esta en una UT
    @Deprecated
    public List<Afiliado> consultarAfiliadosXPeriodo(String periodo) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.estado.cuenta.afiliados.periodo"), params,
                (ResultSet rs, int index) -> {
                    Afiliado afiliado = new Afiliado();
                    afiliado.setDni(rs.getString("DNI"));
                    Cobertura cobertura = new Cobertura();
                    cobertura.setPeriodo(periodo);
                    cobertura.setPoliza(rs.getString("NPOLIZA"));
                    afiliado.setCobertura(cobertura);
                    return afiliado;
                });
    }

    public void actualizar(EstadoCuenta registro) {

        Map<String, Object> params = new HashMap<>(6);
        params.put("cotizacionEsperada", registro.getCotizacion());
        params.put("diasEsperados", registro.getDias());
        params.put("dniEmpleado", registro.getAfiliado().getDni());
        params.put("ibcEsperado", registro.getIbc());
        params.put("periodo", registro.getAfiliado().getCobertura().getPeriodoAnioMes());
        params.put("poliza", registro.getAfiliado().getCobertura().getPoliza());
        params.put("tipoCotizante", registro.getAfiliado().getTipoCotizante());
        params.put("cotizacionReportada", registro.getCotizacionReportada());
        params.put("diasReportados", registro.getDiasReportados());
        params.put("tasaReportada", registro.getTasaReportada());
        params.put("ibcReportado", registro.getIbcReportado());
        params.put("dniModifica",
                registro.getUsuarioOperacion() == null ? getVarEntorno().getValor("usuario.dniingresa")
                        : registro.getUsuarioOperacion());
        params.put("saldo", Objects.isNull(registro.getSaldo()) ? null : registro.getSaldo() * -1);
        params.put("tipoAfiliado", registro.getAfiliado().getTipoAfiliado());
        params.put("centroTrabajo", registro.getCentroTrabajo());
        params.put("centroTrabajoPagador", registro.getCentroTrabajoPagador());
        params.put("tasa", registro.getTasa());

        if (!Objects.isNull(registro.getExistePago())) {
            params.put("tienePago", registro.getExistePago());
        }

        if (!Objects.isNull(registro.getEstadoPago())) {
            params.put("estadoPago", registro.getEstadoPago().getEquivalencia());
        }

        int actualizados = getJdbcTemplate().update(getVarEntorno().getValor("actualizar.item.estado.cuenta.nuevo"),
                params);
        if (actualizados < 1) {
            throw new CambiosEsperadosExcepcion();
        }
    }

    // este metodo solo se usa en una UT?
    public EstadoCuenta consultarXafiliadoPeriodo(String dni, String periodo) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("dni", dni);

        Afiliado af = new Afiliado();
        af.setDni(dni);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.estado.cuenta.xAfiliadoPeriodo"),
                params, (ResultSet rs, int index) -> {
                    return EstadoCuenta.builder().afiliado(af).cotizacion(rs.getLong("PTINGRESO_BASE_LIQ_ESPERADO"))
                            .dias(rs.getInt("NMDIAS_ESPERADOS")).ibc(rs.getLong("PTINGRESO_BASE_LIQ_ESPERADO"))
                            .centroTrabajo(rs.getString("CDSUCURSAL"))
                            .centroTrabajoPagador(rs.getString("CDSUCURSAL_PAGADORA")).build();
                });
    }

    public EstadoCuenta consultarXafiliadoPeriodo(Afiliado afiliado, String periodo) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("dni", afiliado.getDni());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("poliza", afiliado.getCobertura().getPoliza());

        Afiliado af = afiliado;
        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.estado.cuenta.xAfiliadoPeriodo"),
                    params, (ResultSet rs, int index) -> {
                        af.getCobertura().setSucursal(rs.getString("CDSUCURSAL"));
                        af.getCobertura().setSucursalPagadora(rs.getString("CDSUCURSAL_PAGADORA"));

                        return EstadoCuenta.builder().afiliado(af).cotizacion(rs.getLong("PTCOTIZACION_ESPERADA"))
                                .dias(rs.getInt("NMDIAS_ESPERADOS")).ibc(rs.getLong("PTINGRESO_BASE_LIQ_ESPERADO"))
                                .tasa(rs.getDouble("POTASA_ESPERADA")).centroTrabajo(rs.getString("CDSUCURSAL"))
                                .centroTrabajoPagador(rs.getString("CDSUCURSAL_PAGADORA"))
                                .existePago(rs.getString("SNEXISTE_PAGO"))
                                .estadoPago(EstadoPago.estadoPagoPorEquivalencia(rs.getString("CDESTADO_PAGO")))
                                .consecutivo(rs.getLong("NMCONSECUTIVO")).build();
                    });
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void actualizarXcambioDeCT(EstadoCuenta registro, String periodo) {

        Map<String, Object> params = new HashMap<>(6);
        params.put("cotizacion", registro.getCotizacion());
        params.put("ctp", registro.getCentroTrabajoPagador());
        params.put("sucursal", registro.getCentroTrabajo());
        params.put("dni", registro.getAfiliado().getDni());
        params.put("tasa", registro.getTasa());
        params.put("periodo", periodo);
        params.put("poliza", registro.getAfiliado().getCobertura().getPoliza());
        params.put("tipocotizante", registro.getAfiliado().getTipoCotizante());

        int actualizados = getJdbcTemplate().update(getVarEntorno().getValor("actualizar.estadoCuenta_CT"), params);
        if (actualizados < 1) {
            throw new CambiosEsperadosExcepcion();
        }
    }

    public List<String> consultarPeriodosXafiliadoEntrePeriodos(String dni, String periodo, String poliza,
            String tipoCotizante, String periodoInicial, String periodoFinal) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("dni", dni);
        params.put("poliza", dni);
        params.put("tipoCotizante", dni);
        params.put("periodoInicial", periodoInicial);
        params.put("periodoFinal", periodoFinal);

        Afiliado af = new Afiliado();
        af.setDni(dni);

        return getJdbcTemplate().queryForList(getVarEntorno().getValor("consulta.periodos.afectados.cambioCT"), params,
                String.class);
    }

    @Transactional
    public void marcarPagoDeAfiliados(Cobertura cobertura, Date fechaProceso) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", cobertura.getPoliza());
        params.put("fechaProceso", fechaProceso);
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        getJdbcTemplate().update(getVarEntorno().getValor("marcar.pago.estado.cuenta.afiliados"), params);
    }

    public void marcarPagoDeAfiliado(Long numFormulario, Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("numFormulario", numFormulario);
        params.put("poliza", afiliado.getCobertura().getPoliza());
        params.put("periodo", afiliado.getCobertura().getPeriodo());
        params.put("dniAfiliado", afiliado.getDni());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        getJdbcTemplate().update(getVarEntorno().getValor("marcar.pago.estado.cuenta.afiliado"), params);
    }

    @Transactional
    public void marcarEstadosCuentaComoPagados(Afiliado afiliado, EstadoCuenta estadoPagado, Long numeroFormulario) {

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("poliza", afiliado.getCobertura().getPoliza());
        params.put("dni", afiliado.getDni());
        params.put("nmperiodo", afiliado.getCobertura().getPeriodoAnioMes());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());
        params.put("formularioPago", numeroFormulario);
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));
        params.put("existePago", estadoPagado.getExistePago());
        params.put("estadoPago", estadoPagado.getEstadoPago().getEquivalencia());
        params.put("saldo", Objects.isNull(estadoPagado.getSaldo()) ? null : estadoPagado.getSaldo() * -1);
        params.put("ibc", estadoPagado.getIbc());
        params.put("cotizacion", estadoPagado.getCotizacion());
        params.put("cotizacionReportada", estadoPagado.getCotizacionReportada());
        params.put("ibcReportado", estadoPagado.getIbcReportado());
        params.put("diasReportados", estadoPagado.getDiasReportados());
        params.put("tasaReportada", estadoPagado.getTasaReportada());

        try {
            getJdbcTemplate().update(getVarEntorno().getValor("marcar.pago.estados.cuenta.afiliado"), params);
        } catch (DataAccessException e) {
            LOG.info("Error al actualizar Estado de Cuenta ->   ", e);
        }

    }

    public List<EstadoCuenta> consultarEstadosCuentaXAfiliado(Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dni", afiliado.getDni());
        if (!afiliado.getTipoAfiliado().isEmpty()) {
            params.put("tipoAfiliado", afiliado.getTipoAfiliado());
        }
        params.put("nmperiodo", afiliado.getCobertura().getPeriodoAnioMes());

        try {
            return (getJdbcTemplate().query(getVarEntorno().getValor("consulta.estados.cuenta.afiliado"), params,
                    (ResultSet rs, int index) -> {

                        Afiliado afiliadoRespuesta = new Afiliado();

                        Cobertura cobertura = new Cobertura();

                        afiliadoRespuesta.setDni(afiliado.getDni());
                        afiliadoRespuesta.setTipoCotizante(afiliado.getTipoCotizante());
                        afiliadoRespuesta.setTipoAfiliado(afiliado.getTipoAfiliado());

                        cobertura.setPoliza(rs.getString("NMPOLIZA"));
                        cobertura.setPeriodo(afiliado.getCobertura().getPeriodo());
                        afiliadoRespuesta.setCobertura(cobertura);
                        afiliadoRespuesta.setTipoCotizante(rs.getString("CDTIPO_COTIZANTE"));
                        ;
                        afiliadoRespuesta.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
                        ;

                        return EstadoCuenta.builder().consecutivo(rs.getLong("NMCONSECUTIVO"))
                                .afiliado(afiliadoRespuesta).tasa(rs.getDouble("POTASA_ESPERADA"))
                                .dias(rs.getInt("NMDIAS_ESPERADOS")).cotizacion(rs.getLong("PTCOTIZACION_ESPERADA"))
                                .ibc(rs.getLong("PTINGRESO_BASE_LIQ_ESPERADO"))
                                .existePago(rs.getString("SNEXISTE_PAGO"))
                                .cotizacionReportada(rs.getLong("PTCOTIZACION_REPORTADA"))
                                .ibcReportado(rs.getLong("PTINGRESO_BASE_LIQ_REPORTADO"))
                                .diasReportados(rs.getInt("NMDIAS_REPORTADOS"))
                                .tasaReportada(rs.getDouble("POTASA_REPORTADA"))
                                .estadoPago(EstadoPago.estadoPagoPorEquivalencia(rs.getString("CDESTADO_PAGO")))
                                .build();
                    }));
        } catch (DataAccessException e) {
            return null;
        }

    }

    public List<Afiliado> obtenerSumatoriaIbcPorIndependiente(String periodo, String cadenaPolizas, Integer nsmmlv) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("polizas", cadenaPolizas);
        params.put("numerosSmmlv", nsmmlv);

        return getJdbcTemplate().query(getVarEntorno().getValor("consultar.ibc.afiliado.independiente"), params,
                (ResultSet rs, int index) -> {
                    Afiliado afiliado = new Afiliado();
                    afiliado.setDni(rs.getString("DNI_AFILIADO"));
                    afiliado.setTipoCotizante(rs.getString("CDTIPO_COTIZANTE"));
                    afiliado.setUltimoIbc(rs.getDouble("IBC"));
                    afiliado.setPeriodoCotizacion(periodo);
                    return afiliado;
                });
    }

    @Transactional
    public void marcarPagoIndependiente(Afiliado afiliado) {

        Map<String, Object> params = obtenerMapaParametrosActualizacion(afiliado);
        getJdbcTemplate().update(getVarEntorno().getValor("marcar.pago.estadoCuenta.independiente"), params);
    }

    @Transactional
    public void actualizarEstado(Afiliado afiliado, EstadoPago estado, EstadoCuenta estadoCuenta) {

        String sql = getVarEntorno().getValor("actualizar.estado.estadoCuenta");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", afiliado.getCobertura().getPeriodo());
        params.put("dni", afiliado.getDni());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("poliza", afiliado.getCobertura().getPoliza());
        params.put("estadoPago", estado.getEquivalencia());
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        if (EstadoPago.ENRIQUES.equals(estadoCuenta.getEstadoPago())) {
            params.put("saldo", estadoCuenta.getCotizacion());
            sql = getVarEntorno().getValor("actualizar.estado.estadoCuenta.enrqs");
        }

        getJdbcTemplate().update(sql, params);
    }

    @Transactional
    public void marcarPagoIndependiente(List<Afiliado> afiliados) {

        if (Objects.isNull(afiliados) || afiliados.size() == 0) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object>[] params = new HashMap[afiliados.size()];

        int i = 0;
        for (Afiliado afiliado : afiliados) {
            params[i] = obtenerMapaParametrosActualizacion(afiliado);
            i++;
        }
        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("marcar.pago.estadoCuenta.independiente"), params);
    }

    private Map<String, Object> obtenerMapaParametrosActualizacion(Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dniAfiliado", afiliado.getDni());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("periodo", afiliado.getPeriodoCotizacion());
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        return params;
    }

    public String getUltimoPeriodo(String poliza, String afiliado, String tipoCotizante, Date fechaBase) {

        String query = "buscar.ultimo.periodo";
        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        if (fechaBase != null) {
            query = "buscar.ultimo.periodo.fechaBase";
            params.put("fechaBase", fechaBase);
        }

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor(query), params, String.class);
    }

    public void backupEstadoCuenta(String poliza, String afiliado, String tipoCotizante, String periodoInicial,
            String periodoFinal, String usuario) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        params.put("periodoInicial", periodoInicial);
        params.put("periodoFinal", periodoFinal);
        params.put("usuario", usuario);

        getJdbcTemplate().update(getVarEntorno().getValor("insertar.backup.estado.cuenta"), params);

    }

    public void backupTrazaEstadoCuenta(String poliza, String afiliado, String tipoCotizante, String periodoInicial,
            String periodoFinal, String usuario) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        params.put("periodoInicial", periodoInicial);
        params.put("periodoFinal", periodoFinal);
        params.put("usuario", usuario);

        getJdbcTemplate().update(getVarEntorno().getValor("insertar.backup.traza.estado.cuenta"), params);
    }

    public void borrarTrazaEstadoCuenta(String poliza, String afiliado, String tipoCotizante, String periodoInicial,
            String periodoFinal) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        params.put("periodoInicial", periodoInicial);
        params.put("periodoFinal", periodoFinal);

        getJdbcTemplate().update(getVarEntorno().getValor("borrar.backup.traza.estado.cuenta"), params);
    }

    public void borrarEstadoCuenta(String poliza, String afiliado, String tipoCotizante, String tipoAfiliado,
            String periodoInicial, String periodoFinal) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("poliza", poliza);
        params.put("periodoInicial", periodoInicial);
        params.put("periodoFinal", periodoFinal);

        getJdbcTemplate().update(getVarEntorno().getValor("borrar.backup.estado.cuenta"), params);
    }

    public String getPrimerPeriodo(String poliza, String afiliado, String tipoCotizante, Date fechaBase) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        params.put("fechaBase", fechaBase);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("buscar.primer.periodo"), params,
                String.class);
    }

    public List<String> getPeriodosCobertura(String poliza, String afiliado, String tipoCotizante, String tipoAfiliado,
            Date fechaInicial, Date fechaFinal) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", afiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("poliza", poliza);
        params.put("fechaInicial", fechaInicial);
        params.put("fechaFinal", fechaFinal);

        return getJdbcTemplate().queryForList(getVarEntorno().getValor("consulta.rangoPeriodos.cuerpoPoliza"), params,
                String.class);

    }

    @Transactional
    public void merge(EstadoCuenta registro) {

        Map<String, Object> params = obtenerParametros(registro);
        params.putIfAbsent("periodo", registro.getAfiliado().getCobertura().getPeriodoAnioMes());
        params.put("fechaLimite", registro.getFechaLimitePago());

        int actualizados = getJdbcTemplate().update(getVarEntorno().getValor("merge.estadocuenta"), params);
//        if (actualizados < 1) {
//            throw new CambiosEsperadosExcepcion();
//        }
    }

    public Boolean existe(String poliza, String dniEmpleado, String periodo, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dniEmpleado", dniEmpleado);
        params.put("periodo", periodo);
        params.put("tipoAfiliado", tipoAfiliado);

        List<Integer> result = getJdbcTemplate().query(getVarEntorno().getValor("existe.estadocuenta"), params,
                (rs, i) -> {
                    return rs.getInt("CTA");
                });

        return !result.isEmpty();
    }

    @Transactional
    public void actualizarNoPagosAenriques(String poliza, String periodo) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("npoliza", poliza);
        params.put("periodo", periodo);
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));
        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.nopagos.enriques"), params);
    }

    public void borrarDetalle(String poliza, String dni, String tipoAfiliado, String periodoInicial,
            String periodoFinal) {

        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", dni);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("poliza", poliza);
        params.put("periodoInicial", periodoInicial);
        params.put("periodoFinal", periodoFinal);

        getJdbcTemplate().update(getVarEntorno().getValor("borrar.estado.cuenta.detalle"), params);
    }

    public void borrarControl(String poliza, String periodo) {
        Map<String, Object> params = new HashMap<>();

        params.put("poliza", poliza);
        params.put("periodo", periodo);

        getJdbcTemplate().update(getVarEntorno().getValor("borrar.estado.cuenta.control"), params);
    }

    public List<ObjResultadoDtlle> obtenerEstadosCuentaAfectadasPagoIndependiente(Afiliado afiliado, String poliza,
            String periodo) {
        Map<String, Object> params = new HashMap<>();

        params.put("dni", afiliado.getDni());
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        
        try {
            return getJdbcTemplate().query(getVarEntorno().getValor("obtener.ec.afectados.pago.independientes"), params,
                    (ResultSet rs, int index) -> {
                        ObjResultadoDtlle obj = new ObjResultadoDtlle();
                        obj.setConsecutivoEstadoCuenta(rs.getLong("NMCONSECUTIVO_EC"));
                        obj.setConsecutivo(rs.getLong("NMCONSECUTIVO"));
                        obj.setPeriodo(rs.getString("NMPERIODO"));
                        obj.setPoliza(rs.getString("NMPOLIZA"));
                        return obj;
                    });
        } catch (DataAccessException e) {
            return new ArrayList<ObjResultadoDtlle>();
        }

    }

    public List<Long> obtenerFormulariosAfectadosPagoIndependiente(Afiliado afiliado, String poliza,
            String periodo) {
        Map<String, Object> params = new HashMap<>();

        params.put("dni", afiliado.getDni());
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        
        try {
            return getJdbcTemplate().query(getVarEntorno().getValor("obtener.formularios.afectados.pago.independientes"), params,
                (ResultSet rs, int index) -> {
                    return rs.getLong("NMFORMULARIO_PAGO");
                });
        } catch (DataAccessException e) {
            return new ArrayList<Long>();
        }

    }
    
    public int[] borrarDetalleEstadosCuentaAfectadasPagoIndependiente(List<ObjResultadoDtlle> estados) {
        Map<String, Object>[] params = new HashMap[estados.size()];

        int i = 0;
        for (ObjResultadoDtlle item : estados) {
            params[i] = mapaParametrosDtlleAfectada(item);
            i++;
        }

        return getJdbcTemplate().batchUpdate(getVarEntorno().getValor("borrar.detalle.ec.afectados.pago.independientes"),
                params);
    }
    
    /*public void borrarDetalleEstadosCuentaAfectadasPagoIndependiente(Afiliado afiliado, String poliza, String periodo) {
        Map<String, Object> params = new HashMap<>();

        params.put("dni", afiliado.getDni());
        params.put("periodo", periodo);
        params.put("poliza", poliza);

        getJdbcTemplate().queryForList(getVarEntorno().getValor("borrar.detalle.ec.afectados.pago.independientes"),
                params, Long.class);
    }*/

    public int[] desmarcarPagosECAfectadasPagoIndependiente(List<ObjResultadoDtlle> estados) {

        Map<String, Object>[] params = new HashMap[estados.size()];
        int i = 0;
        for (ObjResultadoDtlle item : estados) {
            params[i] = mapaParametrosECAfectada(item.getConsecutivoEstadoCuenta(), "03");
            i++;
        }

        return getJdbcTemplate().batchUpdate(getVarEntorno().getValor("desmarcarPago.ec.afectados.pago.independientes"),
                params);
    }

    private Map<String, Object> mapaParametrosECAfectada(Long consecutivo, String estado) {

        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("consecutivo", consecutivo);
        params.put("estado", estado);
        return params;
    }
    
    private Map<String, Object> mapaParametrosDtlleAfectada(ObjResultadoDtlle obj) {

        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("consecutivo", obj.getConsecutivo());
        return params;
    }

    public class ObjResultadoDtlle {
        Long consecutivoEstadoCuenta;
        Long consecutivo;
        String poliza;
        String periodo;

        public Long getConsecutivoEstadoCuenta() {
            return consecutivoEstadoCuenta;
        }

        public void setConsecutivoEstadoCuenta(Long consecutivoEstadoCuenta) {
            this.consecutivoEstadoCuenta = consecutivoEstadoCuenta;
        }

        public Long getConsecutivo() {
            return consecutivo;
        }

        public void setConsecutivo(Long consecutivo) {
            this.consecutivo = consecutivo;
        }

        public String getPoliza() {
            return poliza;
        }

        public void setPoliza(String poliza) {
            this.poliza = poliza;
        }

        public String getPeriodo() {
            return periodo;
        }

        public void setPeriodo(String periodo) {
            this.periodo = periodo;
        }

    }
}
