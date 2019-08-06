package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.general.accesodatos.QueriesDAO;

@Repository
public class CoberturaDao extends AbstractDAO {

    @Autowired
    QueriesDAO queriesDAO;

    public List<Cobertura> consultarCoberturasNotificacionEmpresa(String periodo) {

        String sql = queriesDAO.getQuery("obtener.coberturas.notificacionEmpresas");
        // String sql =
        // getVarEntorno().getValor("obtener.coberturas.notificacion.empresas");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);

        return getJdbcTemplate().query(sql, params, (ResultSet rs, int index) -> {
            Cobertura cobertura = new Cobertura(rs.getString("NPOLIZA"), periodo);
            return cobertura;
        });
    }

    public List<Cobertura> consultarCoberturasNotificacionVoluntarios(String periodo) {

        String sql = queriesDAO.getQuery("obtener.coberturas.notificacionVoluntarios");
        // String sql =
        // getVarEntorno().getValor("obtener.coberturas.notificacion.voluntarios");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);

        return getJdbcTemplate().query(sql, params, (ResultSet rs, int index) -> {
            Cobertura cobertura = new Cobertura(rs.getString("NPOLIZA"), periodo);
            return cobertura;
        });
    }

    public List<Cobertura> consultarCoberturasNotificacionFechaLimitePago(String periodo) {

        String sql = queriesDAO.getQuery("obtener.coberturas.notificacionFechaLimitePago");
        // String sql =
        // getVarEntorno().getValor("obtener.coberturas.notificacionFechaLimitePago");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);

        return getJdbcTemplate().query(sql, params, (ResultSet rs, int index) -> {
            Cobertura cobertura = new Cobertura(rs.getString("NPOLIZA"), periodo);
            cobertura.setTieneAfiliacion(rs.getString("SNAFILIACION"));
            cobertura.setTipoPoliza(rs.getString("TIPOPOLIZA"));
            return cobertura;
        });
    }

    /**
     * Busca las coberturas relacionadas con sus condiciones
     * 
     * @param dni
     * @param poliza
     * @param periodo
     * @return
     */
    public List<DatosCobertura> consultarDatosCoberturaAfiliados(String dni, String poliza, String periodo) {

        String sql = getVarEntorno().getValor("obtener.condiciones.afiliados");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        params.put("dni", dni);

        return getJdbcTemplate().query(sql, params, (ResultSet rs, int index) -> {
            Condicion condicion = new Condicion();
            condicion.setIndicadorDias(rs.getString("SNINDICADOR_PROPORCIONAL_DIAS"));
            condicion.setTipoGeneracion(rs.getString("CDTIPO_GENERACION"));
            condicion.setTipoNovedad(rs.getString("CDTIPO_NOVEDAD"));
            condicion.setTipoTasa(rs.getString("CDTIPO_TASA"));
            condicion.setIbcMaximo(rs.getInt("PTINGRESO_MAX_BASE_LIQ"));
            condicion.setIbcMinimo(rs.getInt("PTINGRESO_MIN_BASE_LIQ"));
            condicion.setTipoCotizante(rs.getString("CDTIPO_COTIZANTE"));
            condicion.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));

            Cobertura cobertura = new Cobertura();
            cobertura.setUltimoIbcCotizado(rs.getLong("PTULTIMO_IBC_COTIZADO"));
            cobertura.setSalario(rs.getLong("PTSALARIO"));

            return new DatosCobertura(cobertura, condicion);
        });
    }

    public Date obtenerDiaHabil(String periodo, Integer dia) {

        String sql = getVarEntorno().getValor("obtener.dia.habil");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("dia", dia);

        return getJdbcTemplate().queryForObject(sql, params, Date.class);

    }

    public void actualizarCTCuerpolizaRiesgo(String poliza, String dni, Long certificado, String sucursal,
            Date febaja) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dni", dni);
        params.put("certificado", certificado);
        params.put("sucursal", sucursal);
        params.put("febaja", febaja);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.cuerpolizaRiesgo.cambioCT"), params);
    }

    public void setQueriesDAO(QueriesDAO queriesDAO) {
        this.queriesDAO = queriesDAO;
    }

    public class DatosCobertura {
        Cobertura cobertura;
        Condicion condicion;

        public Cobertura getCobertura() {
            return cobertura;
        }

        public void setConbertura(Cobertura cobertura) {
            this.cobertura = cobertura;
        }

        public Condicion getCondicion() {
            return condicion;
        }

        public void setCondicion(Condicion condicion) {
            this.condicion = condicion;
        }

        public DatosCobertura(Cobertura cobertura, Condicion condicion) {
            super();
            this.cobertura = cobertura;
            this.condicion = condicion;
        }

    }
}
