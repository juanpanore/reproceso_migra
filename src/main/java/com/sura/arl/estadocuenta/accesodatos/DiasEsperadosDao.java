package com.sura.arl.estadocuenta.accesodatos;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class DiasEsperadosDao extends AbstractDAO {
    private static final Logger LOG = LoggerFactory.getLogger(DiasEsperadosDao.class);

    public Integer consultarDiasEsperados(String poliza, String dniAfiliado, String periodo, String indicadorDias,
            String tipoNovedad, String tipoCotizante, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("dniAfiliado", dniAfiliado);
        params.put("periodo", periodo);
        params.put("indicadorDias", indicadorDias);
        params.put("tipoNovedad", tipoNovedad);
        params.put("tipoCotizante", Objects.isNull(tipoCotizante) ? tipoCotizante : Arrays.asList(tipoCotizante.split(",")));
        params.put("tipoAfiliado", tipoAfiliado);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.dias.esperada"), params,
                (ResultSet rs, int index) -> {
                    return rs.getInt("DIAS_ESPERADOS");
                });

    }
    
     public Integer consultarMultipleCobertura(String poliza, String dniAfiliado,
            String periodo, String tipoCotizante, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dniAfiliado", dniAfiliado);
        params.put("periodo", periodo);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.multiple.cobertura"), params,
                (ResultSet rs, int index) -> {
                    return rs.getInt("COBERTURA");
                });

    }

}