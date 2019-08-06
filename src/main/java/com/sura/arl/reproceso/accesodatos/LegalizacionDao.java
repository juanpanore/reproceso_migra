package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class LegalizacionDao extends AbstractDAO {

    public Double obtenerTotalPagado(String poliza, String periodo) {

        //TODO: ir a autoliquidaciones - ptcotizacion_a_pagar
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodo", periodo.substring(4,6).concat(periodo.substring(0,4)));
        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.total.pagado.legalizaciones"), params,
                Double.class);
    }

}
