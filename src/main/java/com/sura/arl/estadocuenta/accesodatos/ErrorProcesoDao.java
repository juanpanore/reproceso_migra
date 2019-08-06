package com.sura.arl.estadocuenta.accesodatos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Repository;

import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.ErrorProceso.EstadoError;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;
import com.sura.arl.reproceso.util.Cadenas;
import com.sura.arl.reproceso.util.Objetos;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class ErrorProcesoDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorProcesoDao.class);

    private static final int NUMERO_PARAMS_REGISTRO = 12;
    static final String REGEX_VALIDACION_PERIODO = "^(0?[1-9]|1[0-2])(19|20)\\d{2}$";

    public void registrar(ErrorProceso errorProceso) {

        Objects.requireNonNull(errorProceso, "Se espera un objeto error proceso");
        Cadenas.requiereNoNuloNoVacio(errorProceso.getCodError(), "El codigo de error no puede ser nulo o vacio");

        Map<String, Object> params = mapaParametros(errorProceso);

        getJdbcTemplate().update(getVarEntorno().getValor("registro.errorproceso"), params);
    }

    @SuppressWarnings("unchecked")
    public void registrarComoLote(List<ErrorProceso> erroresProceso) {

        Objects.requireNonNull(erroresProceso, "Se espera una lista de objetos error proceso");

        Map<String, Object>[] params = new HashMap[erroresProceso.size()];
        int i = 0;
        for (ErrorProceso item : erroresProceso) {
            params[i] = mapaParametros(item);
            i++;
        }

        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("registro.errorproceso"), params);
    }

    public void borrar(String poliza, String periodoCotizacion) {

        Cadenas.requiereNoNuloNoVacio(poliza, "La poliza no puede ser nula o vacia");
        Cadenas.requiereNoNuloNoVacio(periodoCotizacion, "El periodo cotizacion no puede ser nulo o vacio");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodoCotizacion", periodoCotizacion);

        getJdbcTemplate().update(getVarEntorno().getValor("borrar.errorproceso"), params);
    }

    public Integer contarErrores(String poliza, String periodoCotizacion) {

        Cadenas.requiereNoNuloNoVacio(poliza, "La poliza no puede ser nula o vacia");
        Cadenas.requiereNoNuloNoVacio(periodoCotizacion, "El periodo cotizacion no puede ser nulo o vacio");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodoCotizacion", periodoCotizacion);

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.errorproceso.totalErrores"),
                    params, Integer.class);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return -1;
        }

    }
    
    public void registrarUnico(ErrorProceso errorProceso) {

        if (errorProceso == null) {
            throw new AccesoDatosExcepcion("No es posible registrar sin datos");
        }

        if ((errorProceso.getCodError() == null || errorProceso.getCodError().trim().isEmpty())
                || errorProceso.getUsuarioRegistro() == null) {
            throw new AccesoDatosExcepcion("No es posible registrar la entidad sin completar los datos obligatorios");
        }

        Map<String, Object> params = mapaParametros(errorProceso);

        getJdbcTemplate().update(getVarEntorno().getValor("registro.errorproceso.unico"), params);
    }

    private Map<String, Object> mapaParametros(ErrorProceso registro) {

        Objetos.requiereNoNulo(registro, "Se esperan valores para registro de error proceso");
//        Objetos.requiereNoNulo(registro.getEstadoError(), "Se espera valor para el estado");

        Map<String, Object> params = new HashMap<String, Object>(ErrorProcesoDao.NUMERO_PARAMS_REGISTRO);
        params.put("codError", registro.getCodError());
        params.put("dni", registro.getDni());
        params.put("npoliza", registro.getNpoliza());
        params.put("periodo", corregirPeriodo(registro.getPeriodo()));
        params.put("dniIngresa", registro.getUsuarioRegistro());
        params.put("codProceso", registro.getCodigoProceso());
        params.put("nmperiodoGeneracion", corregirPeriodo(registro.getPeriodoGeneracion()));
        params.put("tipoGeneracion", registro.getTipoGeneracion());
        params.put("observacion", registro.getObservacion());
        params.put("estado", registro.getEstadoError() != null ? registro.getEstadoError().name(): null);
        params.put("tipoCotizante", registro.getTipoCotizante());

        if (registro.getEstadoError() == null) {
            registro.setEstadoError(EstadoError.POR_CORREGIR);
        }
        params.put("estado", registro.getEstadoError().name());

        return params;

    }

    /**
     * 
     * Actualiza el estado de un error especifico y unico.
     * 
     * @param poliza
     * @param periodoGeneracion
     * @param periodoCotizacion
     * @param codigoInconsistencia
     * @param nuevoEstado
     * @param dniAfiliado
     */
    public void actualizarEstado(String poliza, String periodoGeneracion, String periodoCotizacion,
            String codigoInconsistencia, EstadoError nuevoEstado, String dniAfiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodoGeneracion", corregirPeriodo(periodoGeneracion));
        params.put("periodoCotizacion", corregirPeriodo(periodoCotizacion));
        params.put("codigoInconsistencia", codigoInconsistencia);
        params.put("nuevoEstado", nuevoEstado.name());
        params.put("dniAfiliado", dniAfiliado);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.estadocuenta.inconsistencia.estado"), params);

    }

    /**
     * 
     * Actualiza el estado de los errores asociados a un afiliado.
     * 
     * @param poliza
     * @param periodoGeneracion
     * @param periodoCotizacion
     * @param nuevoEstado
     * @param dniAfiliado
     */
    public void actualizarEstado(String poliza, String periodoGeneracion, String periodoCotizacion,
            EstadoError nuevoEstado, String dniAfiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodoGeneracion", corregirPeriodo(periodoGeneracion));
        params.put("periodoCotizacion", corregirPeriodo(periodoCotizacion));
        params.put("nuevoEstado", nuevoEstado.name());
        params.put("dniAfiliado", dniAfiliado);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.estadocuenta.inconsistencias.segun.afiliado"),
                params);

    }

    public String corregirPeriodo(String periodo) {
        if (periodo == null) {
            return null;
        }
        
        if(Pattern.matches(REGEX_VALIDACION_PERIODO, periodo)){
            periodo = periodo.substring(2).concat(periodo.substring(0,2));
        }
        return periodo;
    }
}
