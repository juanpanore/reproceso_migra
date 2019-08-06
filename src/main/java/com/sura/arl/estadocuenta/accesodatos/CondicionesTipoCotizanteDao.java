package com.sura.arl.estadocuenta.accesodatos;

import com.sura.arl.estadocuenta.modelo.CondicionesTipoCotizante;
import com.sura.arl.general.accesodatos.AbstractDAO;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

@Repository
public class CondicionesTipoCotizanteDao extends AbstractDAO {

    public CondicionesTipoCotizante obtenerCantidadSalariosTopesIBC(String tipoCotizante) {
        String sql = getVarEntorno().getValor("consulta.topesIBC.condicionesTipoCotizante");

        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("tipoCotizante", tipoCotizante);

        try {
            return getJdbcTemplate().queryForObject(sql, params, (ResultSet rs, int index) -> {
                CondicionesTipoCotizante ctc = new CondicionesTipoCotizante();
                ctc.setCantidadMinimaSalarios(rs.getInt("MINIBC"));
                ctc.setCantidadMaximaSalarios(rs.getInt("MAXIBC"));

                return ctc;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
