package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.TrazaAfectacionSaldo;

@Repository
public class TrazaAfectacionSaldoDao extends AbstractDAO {

    @Transactional
    public void registrar(TrazaAfectacionSaldo traza) {

        Map<String, Object> param = new HashMap<>();
        param.put("poliza", traza.getPoliza());
        param.put("periodo", traza.getPeriodo());
        param.put("observacion", traza.getObservacion());
        param.put("valor", traza.getValor());
        param.put("estado", traza.getEstado().getEquivalencia());
        
        getJdbcTemplate().update(getVarEntorno().getValor("insertar.traza.afectacion.saldo"), param);

    }
}
