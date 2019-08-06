package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class HistoricoMovimientoEstadoCuentaDao extends AbstractDAO {

    @Transactional
    public void generarHistoricoControl(String periodo, String poliza, String fuente, String dni) {

        Map<String, Object> params = new HashMap<>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        /*
         * params.put("dni", dni); params.put("fuente", fuente);
         */

        getJdbcTemplate().update(getVarEntorno().getValor("crear.hco.movimiento.control.estadocuenta"), params);
    }

    @Transactional
    public void generarHistoricoEstadoCuenta(String periodo, String poliza, String fuente, String dni) {

        Map<String, Object> params = new HashMap<>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        /*
         * params.put("dni", dni); params.put("fuente", fuente);
         */

        getJdbcTemplate().update(getVarEntorno().getValor("crear.hco.movimiento.estadocuenta"), params);
    }

    public void generarHistoricos(String periodo, String poliza, String fuente, String dni) {
        generarHistoricoControl(periodo, poliza, fuente, dni);
        generarHistoricoEstadoCuenta(periodo, poliza, fuente, dni);
    }
}
