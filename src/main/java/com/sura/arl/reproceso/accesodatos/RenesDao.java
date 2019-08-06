package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;
import java.util.List;

@Repository
public class RenesDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(RenesDao.class);

    public Integer registrar(Long formulario, String dniAfiliado, String tipoCotizante, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("formulario", formulario);
        params.put("dniAfiliado", dniAfiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("usuarioOperacion", getVarEntorno().getValor("usuario.dniingresa"));

        return getJdbcTemplate().update(getVarEntorno().getValor("ingresar.renes.segun.legalizacion"), params);
    }

    public Integer update(Long formulario, String dniAfiliado, String tipoCotizante, String tipoAfiliado, String poliza, String periodo) {

        Map<String, Object> params = new HashMap<>();
        params.put("formulario", formulario);
        params.put("dniAfiliado", dniAfiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("usuarioOperacion", getVarEntorno().getValor("usuario.dniingresa"));
        params.put("poliza", poliza);
        params.put("periodo", periodo);

        return getJdbcTemplate().update(getVarEntorno().getValor("update.renes"), params);
    }

    public Integer borrar(String poliza, String dniEmpleado, String periodo, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dniEmpleado", dniEmpleado);
        params.put("periodo", periodo);
        params.put("tipoAfiliado", tipoAfiliado);

        return getJdbcTemplate().update(getVarEntorno().getValor("borrar.renes"), params);
    }

    public Integer borrar(String poliza, String dniEmpleado, String periodo, String tipoAfiliado, Long formulario) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dniEmpleado", dniEmpleado);
        params.put("periodo", periodo);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("formulario", formulario);

        return getJdbcTemplate().update(getVarEntorno().getValor("borrar.renes.formulario"), params);
    }

    public Boolean existe(String poliza, String dniEmpleado, String periodo, String tipoAfiliado, Long formulario) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dniEmpleado", dniEmpleado);
        params.put("periodo", periodo);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("formulario", formulario);

        List<Integer> result = getJdbcTemplate().query(getVarEntorno().getValor("consulta.renes.formulario"), params, (rs, i) -> {
            return rs.getInt("RENE");
        });

        return !result.isEmpty();
    }

    public Integer borrarSinCobertura(String poliza, String dniAfiliado, String periodo, String formulario) {
        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dniAfiliado", dniAfiliado);
        params.put("periodo", periodo);
        params.put("formulario", formulario);

        return getJdbcTemplate().update(getVarEntorno().getValor("borrar.renes.formulario.sincobertura"), params);
    }
}
