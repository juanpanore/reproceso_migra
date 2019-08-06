package com.sura.arl.general.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class QueriesDAO extends AbstractDAO {
    /**
     * Devuelve el query almacenado en TREC_QUERIES_PROCESO
     * @param identificador
     * @return
     */
    public String getQuery(String identificador) {
        
        String sql = getVarEntorno().getValor("consulta.contenido.query");

        Map<String, Object> params = new HashMap<>(1);
        params.put("queryid", identificador);

        try {
            return getJdbcTemplate().queryForObject(sql, params, String.class);

        } catch (EmptyResultDataAccessException e) {

            throw e;
        }
    }

}
