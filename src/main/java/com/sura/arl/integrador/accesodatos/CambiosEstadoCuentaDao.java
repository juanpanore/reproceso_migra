package com.sura.arl.integrador.accesodatos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.general.accesodatos.QueriesDAO;
import com.sura.arl.integrador.exceptions.IntegradorEsperadasExcepcion;
import com.sura.arl.integrador.modelo.EstadoIntegrador;
import com.sura.arl.integrador.modelo.Registro;

/**
 *
 * @author pragma.co
 */
@Repository
public class CambiosEstadoCuentaDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(CambiosEstadoCuentaDao.class);

    @Autowired
    private QueriesDAO queriesDAO;

    @Transactional
    public Integer actualizarRegistroATramitar(Long consecutivo, EstadoIntegrador estado, String dniModifica) {

        Map<String, Object> params = new HashMap<>();
        params.put("consecutivo", consecutivo);
        params.put("estado", estado.name());
        params.put("dniModifica", dniModifica);

        int update = getJdbcTemplate().update(getVarEntorno().getValor("reproceso.marcar.cambios.estado.cuenta"),
                params);
        if (estado == EstadoIntegrador.PROCESADO) {
            LOG.debug(String.format("** FIN PROCESO %s ", consecutivo));
        }
        return update;
    }

    public String getFuente(Long consecutivo) {

        Map<String, Object> params = new HashMap<>();
        params.put("consecutivo", consecutivo);

        List<String> list = getJdbcTemplate().query(getVarEntorno().getValor("cambios.estado.cuenta.cdfuente"), params,
                (rs, i) -> {
                    return rs.getString("CDFUENTE");
                });

        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional
    public void escogerRegistrosAGestionar() {

        Map<String, Object> params = new HashMap<>();
        params.put("limite", Integer.parseInt(getVarEntorno().getValor("negocio.numero.registros")));
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

        getJdbcTemplate().update(getVarEntorno().getValor("escoger.cambios.estado.cuenta"), params);
    }

    /**
     * Metodo encargado de consultar los registros establecidos en la tabla
     * TCPG_INTEGRA_ESTADO_CTA
     *
     * @return Listado de Registros a tramitar.
     *
     * @throws IntegradorEsperadasExcepcion
     */
    public List<Registro> consultarCambiosEstadoCuenta(Double inicio, Double fin) throws IntegradorEsperadasExcepcion {

        Map<String, Object> params = new HashMap<>();
        //params.put("limite", Integer.parseInt(getVarEntorno().getValor("negocio.numero.registros")));
        params.put("inicio",inicio);
        params.put("fin",fin);
        //String sql = queriesDAO.getQuery("consultar.cambios.estado.cuenta");
        String sql = " SELECT * FROM ( SELECT CEC.NMCONSECUTIVO, CEC.NMPOLIZA, CEC.DNI, CEC.DSPARAMETROS , ROW_NUMBER() OVER (ORDER BY NMCONSECUTIVO) Row_Num FROM TCPG_INTEGRA_ESTADO_CTA CEC WHERE CDESTADO IN('NUEVO','ENCOLA') AND SUBSTR(DSPARAMETROS,\r\n" + 
        		"                INSTR(dsparametros, 'tipo:') + 5,\r\n" + 
        		"                INSTR(SUBSTR(DSPARAMETROS,INSTR(dsparametros, 'tipo:') + 5), ',')-1) IN ('AFILIACION','RETIRO','MOVER_COBERTURA')\r\n" + 
        		"                ) WHERE Row_Num BETWEEN :inicio and :fin";

        return getJdbcTemplate().query(sql, params, (ResultSet rs, int index) -> {

            Registro registro = new Registro();
            registro.setId(rs.getLong("NMCONSECUTIVO"));
            registro.setDni(rs.getString("DNI"));
            registro.setPoliza(rs.getString("NMPOLIZA"));
            registro.setDsParametros(rs.getString("DSPARAMETROS"));
            return registro;
        });

    }
    
    public List<Registro> consultarCambiosEstadoCuenta() throws IntegradorEsperadasExcepcion {

        Map<String, Object> params = new HashMap<>();
        params.put("limite", Integer.parseInt(getVarEntorno().getValor("negocio.numero.registros")));

        String sql = queriesDAO.getQuery("consultar.cambios.estado.cuenta");

        return getJdbcTemplate().query(sql, params, (ResultSet rs, int index) -> {

            Registro registro = new Registro();
            registro.setId(rs.getLong("NMCONSECUTIVO"));
            registro.setDni(rs.getString("DNI"));
            registro.setPoliza(rs.getString("NMPOLIZA"));
            registro.setDsParametros(rs.getString("DSPARAMETROS"));
            return registro;
        });

    }

    /**
     * Metodo encargado de actualizar un registro en la tabla
     * TCPG_INTEGRA_ESTADO_CTA con el estado 'RET' (Registro en tramite)
     *
     * @param registro, objeto a actualizar.
     */
    @Transactional
    public void actualizarRegistroATramitar(Registro registro) throws IntegradorEsperadasExcepcion {

        Map<String, Object> params = obtenerParametros(registro);

        getJdbcTemplate().update(getVarEntorno().getValor("marcar.cambios.estado.cuenta"), params);
    }

    /**
     * Metodo encargado de actualizar un registro en la tabla
     * TCPG_INTEGRA_ESTADO_CTA con el estado 'ENPROCESO'
     *
     * @param registros
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public void actualizarRegistrosATramitar(List<Registro> registros) throws IntegradorEsperadasExcepcion {

        if (registros.isEmpty()) {
            return;
        }

        int i = 0;
        Map<String, Object> params[] = new Map[registros.size()];

        for (Registro registro : registros) {
            Map<String, Object> param = obtenerParametros(registro);

            params[i++] = param;
        }

        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("marcar.cambios.estado.cuenta"), params);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ingresarRegistroATramitar(Registro registro, String dniIngresa) throws IntegradorEsperadasExcepcion {

        Map<String, Object> params = new HashMap<>();
        params.put("consecutivo", registro.getId());
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("parametros", registro.getDsParametros());
        params.put("estado", registro.getEstado());
        params.put("dniIngresa", dniIngresa);
        
        getJdbcTemplate().update(getVarEntorno().getValor("ingresar.cambios.estado.cuenta"), params);
    }

    public Long getProximaSecuencia() {

        Map<String, Object> params = new HashMap<>();

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("obtener.secuencia"), params, Long.class);
    }

    private Map<String, Object> obtenerParametros(Registro registro) {

        Map<String, Object> params = new HashMap<>();
        params.put("consecutivo", registro.getId());
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("mensaje", registro.getMensajeMQ());
        params.put("estado", registro.getEstado());
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));
        return params;
    }
}
