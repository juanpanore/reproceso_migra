package com.sura.arl.estadocuenta.accesodatos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.estadocuenta.modelo.DatosTasa;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class TasaEsperadaDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(TasaEsperadaDao.class);

    public DatosTasa consultarTasa(String poliza, String periodo, String dniAfiliado, String tipoTasa,
            String tipoCotizante, String tipoAfiliado) {

        Map<String, Object> params = obtenerParametros(poliza, periodo, dniAfiliado, tipoTasa, tipoCotizante,
                tipoAfiliado);
        
        DatosTasa d = getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.tasa.esperada"), params,
                (ResultSet rs, int index) -> {
                    return obtenerResultados(rs.getDouble("tasaCalculada"), rs.getString("CDSUCURSAL"),
                            rs.getString("CDSUCURSAL_PAGADORA"));
                });
        return d;

    }

    private Map<String, Object> obtenerParametros(String poliza, String periodo, String dniAfiliado, String tipoTasa,
            String tipoCotizante, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("dniAfiliado", dniAfiliado);
        params.put("periodo", periodo);
        params.put("tipoTasa", tipoTasa);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        return params;
    }

    private DatosTasa obtenerResultados(Double tasaCalculada, String centroTrabajo, String centroTrabajoPagador) {

        DatosTasa datos = new DatosTasa();
        datos.setTasaCalculada(tasaCalculada);
        datos.setCentroTrabajo(centroTrabajo);
        datos.setCentroTrabajoPagador(centroTrabajoPagador);
        return datos;
    }

}