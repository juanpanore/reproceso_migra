package com.sura.arl.estadocuenta.accesodatos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.estadocuenta.modelo.EstadoCuentaMetadata;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class EstadoCuentaMetadataDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(EstadoCuentaMetadataDao.class);

    public void insertar(EstadoCuentaMetadata metadata) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", metadata.getPoliza());
        params.put("periodo", metadata.getPeriodo());
        params.put("dniAfiliado", metadata.getDniAfiliado());
        params.put("tipoCotizante", metadata.getTipoCotizante());
        params.put("campo", metadata.getCampo());
        params.put("valor", metadata.getValor());
        params.put("dniIngresa", metadata.getDniIngresa());

        final String sql = getVarEntorno().getValor("insertar.estadoCuenta.metadata");
        getJdbcTemplate().update(sql, params);
    }

    public String obtenerCampo(EstadoCuentaMetadata metadata) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", metadata.getPoliza());
        params.put("periodo", metadata.getPeriodo());
        params.put("dniAfiliado", metadata.getDniAfiliado());
        params.put("tipoCotizante", metadata.getTipoCotizante());
        params.put("campo", metadata.getCampo());

        final String sql = getVarEntorno().getValor("consultar.estadoCuenta");

        try {
            return getJdbcTemplate().queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException e) {
            LOG.trace(
                    "No se encontro metadata con poliza:" + metadata.getPoliza() + ", periodo: " + metadata.getPeriodo()
                            + ", dniAfiliado: " + metadata.getDniAfiliado() + ",campo: " + metadata.getCampo(),
                    e);
            return null;
        }
    }

    public List<Map<String, Object>> obtenerTodosLosCampos(EstadoCuentaMetadata metadata) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", metadata.getPoliza());
        params.put("periodo", metadata.getPeriodo());
        params.put("dniAfiliado", metadata.getDniAfiliado());
        params.put("tipoCotizante", metadata.getTipoCotizante());

        final String sql = getVarEntorno().getValor("consultar.estadoCuenta.todosloscampos");

        try {
            return getJdbcTemplate().queryForList(sql, params);
        } catch (EmptyResultDataAccessException e) {
            LOG.trace(
                    "No se encontro metadata con poliza:" + metadata.getPoliza() + ", periodo: " + metadata.getPeriodo()
                            + ", dniAfiliado: " + metadata.getDniAfiliado() + ",campo: " + metadata.getCampo(),
                    e);
            return null;
        }

    }

}
