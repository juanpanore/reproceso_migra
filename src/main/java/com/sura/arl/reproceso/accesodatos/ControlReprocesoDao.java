package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class ControlReprocesoDao extends AbstractDAO {

    public List<Long> consultarFormulariosPendientesReproceso() {

        Map<String, Object> params = new HashMap<String, Object>();

        return getJdbcTemplate().query(getVarEntorno().getValor("buscar.formularios.pendientes.reproceso"), params,
                (ResultSet rs, int index) -> {
                    return rs.getLong("NMFORMULARIO_PAGO");
                });
    }

    public void registrar(Long numeroFormulario, EstadoReproceso estado) {

        Map<String, Object> param = new HashMap<>();
        param.put("numeroFormulario", numeroFormulario);
        param.put("estado", estado);

        getJdbcTemplate().update(getVarEntorno().getValor("ingresar.registro.controlReproceso"), param);

    }

    public enum EstadoReproceso {
        PENDIENTE, PROCESADO
    }
}
