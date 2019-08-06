package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.util.UtilCache;

@Repository
public class ParametrosDao extends AbstractDAO {

    @Deprecated
    public Parametro consultar(String codigo) {

        Map<String, Object> params = new HashMap<>();
        params.put("codigo", codigo);

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.parametro.codigo"), params,
                    (ResultSet rs, int index) -> Parametro.crear(codigo, rs.getString("valor")));
        } catch (DataAccessException e) {
            return null;
        }

    }

    @Deprecated
    public void actualizar(String codigo, String valor) {
        Map<String, Object> params = new HashMap<>();
        params.put("codigo", codigo);
        params.put("valor", valor);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.parametro"), params);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> obtenerTodosParametros() {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fechaHoy = sdf.format(new Date());
        String objCache = "PARAMS_"+fechaHoy;
        
        if (!Objects.isNull(UtilCache.obtener(objCache))) {
            return (Map<String, String>) UtilCache.obtener(objCache);
        }

        try {
            return getJdbcTemplate().query(getVarEntorno().getValor("obtener.parametros"),
                    new ResultSetExtractor<Map>() {
                        @Override
                        public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                            HashMap<String, String> mapRet = new HashMap<String, String>();
                            while (rs.next()) {
                                mapRet.put(rs.getString("CDPARAMETRO"), rs.getString("DSVALOR"));
                            }
                            UtilCache.agregar(objCache, mapRet);
                            return mapRet;
                        }
                    });
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Parametro {
        private final String codigo;

        private final String valor;

        private Parametro(String codigo, String valor) {
            super();
            this.codigo = codigo;
            this.valor = valor;
        }

        public static Parametro crear(String codigo, String valor) {
            return new Parametro(codigo, valor);
        }

        public String getCodigo() {
            return codigo;
        }

        public String getValor() {
            return valor;
        }

    }
}
