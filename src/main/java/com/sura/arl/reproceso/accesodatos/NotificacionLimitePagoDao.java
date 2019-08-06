package com.sura.arl.reproceso.accesodatos;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.general.accesodatos.QueriesDAO;
import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.util.UtilCadenas;
import com.sura.arl.reproceso.util.ValidacionEmail;
import com.sura.arl.reproceso.util.VariablesEntorno;

@Repository
public class NotificacionLimitePagoDao extends AbstractDAO {

    private final String INCONSISTENCIA_NO_AFILIADOS = "EL CONTRATO NO TIENE AFILIADOS PARA EL PERIODO";
    private final String INCONSISTENCIA_NO_CORREO = "NO SE ENCONTRO CORREO DE NOTIFICACION";
    private final String INCONSISTENCIA_CORREO_INVALIDO = "CORREO DE NOTIFICACION INVALIDO";
    private static final Logger LOG = LoggerFactory.getLogger(NotificacionLimitePagoDao.class);
    private ValidacionEmail validacionEmailServicio;
    private QueriesDAO queriesDAO;
    
    
    @Autowired
    public NotificacionLimitePagoDao(ValidacionEmail validacionEmailServicio, QueriesDAO queriesDAO) {
        this.validacionEmailServicio = validacionEmailServicio;
        this.queriesDAO = queriesDAO;
    }

    public Optional<Date> consultaFechaLimitePago(String poliza, String periodo) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodo", periodo);

        try {
            return Optional.ofNullable(getJdbcTemplate().queryForObject(
                    getVarEntorno().getValor("obtener.fechaLimitePago.polizaXPeriodo"), params, Date.class));
        } catch (DataAccessException dae) {
            LOG.error("Ha ocurrido un error al calcular fecha limiete pago {}", dae);
            return Optional.empty();
        }
    }

    public boolean existeFechaFestiva(Date fecha) {

        Map<String, Object> params = new HashMap<>();
        params.put("fecha", fecha);

        try {
            getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.validacion.diafestivo"), params,
                    Date.class);
            return true;

        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Transactional
    public void ingresar(NotificacionLimitePago registro) {

        Map<String, Object> params = obtenerMapaParametrosIngreso(registro);

        getJdbcTemplate().update(getVarEntorno().getValor("ingreso.notificacion.limitePago"), params);
    }

    @Transactional
    public void ingresar(List<NotificacionLimitePago> registros) {

        if (registros.isEmpty()) {
            return;
        }

        Map<String, Object>[] params = new HashMap[registros.size()];

        int i = 0;
        for (NotificacionLimitePago registro : registros) {
            params[i] = obtenerMapaParametrosIngreso(registro);
            i++;
        }

        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("ingreso.notificacion.limitePago"), params);
    }

    @Transactional
    public void actualizar(NotificacionLimitePago registro) {

        Map<String, Object> params = obtenerMapaParametrosActualizacion(registro);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizacion.notificacion.limitePago"), params);
    }

    @Transactional
    public void actualizar(List<NotificacionLimitePago> registros) {

        Map<String, Object>[] params = new HashMap[registros.size()];

        int i = 0;
        for (NotificacionLimitePago registro : registros) {
            params[i] = obtenerMapaParametrosActualizacion(registro);
            i++;
        }

        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("actualizacion.notificacion.limitePago"), params);
    }

    private Map<String, Object> obtenerMapaParametrosActualizacion(NotificacionLimitePago registro) {

        Map<String, Object> params = new HashMap<>();
        params.put("estado", registro.getEstadoNotificacion().name());
        params.put("fechaNotificacion", registro.getFechaNotificacion());
        params.put("nombreArchivo", registro.getNombreArchivo());
        params.put("usuarioRegistro", getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA));
        params.put("consecutivo", registro.getConsecutivo());
        params.put("correoNotificacion", registro.getCorreoAfiliado().orElse(null));
        params.put("nombreAfiliado", registro.getNombreAfiliado());
        params.put("dniAportante", registro.getDni());

        return params;
    }

    private Map<String, Object> obtenerMapaParametrosIngreso(NotificacionLimitePago registro) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", registro.getPoliza());
        params.put("estado", registro.getEstadoNotificacion().name());
        params.put("periodo", registro.getPeriodo());
        params.put("usuarioRegistro", getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA));
        params.put("fechaLimitePago", registro.getFechaLimitePago());

        return params;
    }

    public List<NotificacionLimitePago> obtenerContratosPeriodoVencidoANotificarPorFecha(Date fechaLimitePago,
            String periodoPago) {
        
        String sql = queriesDAO.getQuery("consulta.notificaciones.anticipadas");
        //String sql = getVarEntorno().getValor("consulta.notificaciones.anticipadas");
        
        Map<String, Object> params = new HashMap<>();
        params.put("fechaLimitePago", fechaLimitePago);
        params.put("periodoPago", periodoPago);

        return getJdbcTemplate().query(sql, params,
                (rs, rows) -> {

                    NotificacionLimitePago registro = new NotificacionLimitePago();
                    registro.setConsecutivo(rs.getLong("NMCONSECUTIVO"));
                    registro.setCorreoAfiliado(Optional.ofNullable(rs.getString("DSEMAIL")));
                    registro.setDni(rs.getString("DNI"));
                    registro.setFechaLimitePago(fechaLimitePago);
                    registro.setNombreAfiliado(UtilCadenas.normalizarNombreAportante(rs.getString("DSNOMBRE")));
                    registro.setNroAfiliados(rs.getLong("NROAFILIADOS"));
                    registro.setPeriodo(periodoPago);
                    registro.setPoliza(rs.getString("NMPOLIZA"));

                    if (!registro.getCorreoAfiliado().isPresent()) {
                        registro.crearObservacion(CatalogoErrores.CORREO_NOTIFICACION_VACIO, INCONSISTENCIA_NO_CORREO);
                    } else if (!validacionEmailServicio.validarEmail(registro.getCorreoAfiliado().get())) {
                        registro.crearObservacion(CatalogoErrores.CORREO_NOTIFICACION_INVALIDO,
                                INCONSISTENCIA_CORREO_INVALIDO);
                    }

                    if (registro.getNroAfiliados() == 0) {
                        registro.crearObservacion(CatalogoErrores.NRO_AFILIADOS_CERO, INCONSISTENCIA_NO_AFILIADOS);
                    }
                    return registro;
                });

    }

}
