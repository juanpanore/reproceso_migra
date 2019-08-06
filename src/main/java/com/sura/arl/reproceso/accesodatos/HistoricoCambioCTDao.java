package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.HistoricoCambioCT;

@Repository
public class HistoricoCambioCTDao extends AbstractDAO {

    public void insertar(HistoricoCambioCT registro) {

        Map<String, Object> params = new HashMap<>(6);
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("fuente", registro.getFuente());
        params.put("periodosCambio", registro.getPeriodosCambio());
        params.put("sucursalAnterior", registro.getSucursalAnterior());
        params.put("sucursalNueva", registro.getSucursalNueva());
        params.put("dniIngresa", registro.getDniIngresa());

        getJdbcTemplate().update(getVarEntorno().getValor("insertar.historicoCT"), params);
    }

}
