package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class CentroTrabajoPagadorDao extends AbstractDAO {

    public String buscarCTP(String periodo, String poliza, String sucursal) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        params.put("sucursal", sucursal);

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.buscarCTP"), params, String.class);
        } catch (DataAccessException e) {
            return null;
        }
    }

}
