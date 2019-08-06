package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class ReprocesoPendienteDao extends AbstractDAO {

    @Transactional
    public void registrar(String poliza, String periodo, Long formulario) {

        Map<String, Object> param = new HashMap<>();
        param.put("poliza", poliza);
        param.put("periodo", periodo);
        param.put("formulario", formulario);
        

        getJdbcTemplate().update(getVarEntorno().getValor("insertar.reproceso.pendiente"), param);

    }
}
