package com.sura.arl.estadocuenta.accesodatos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.CampoActualizado;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;

@Repository
public class TrazaEstadoCuentaDao extends AbstractDAO {

    private static final int NUMERO_PARAMS_REGISTRO = 8;

    public void registroCampoActualizado(CampoActualizado campo) {

        Map<String, Object> params = mapaParametros(campo);
        getJdbcTemplate().update(getVarEntorno().getValor("registro.campoactualizado"), params);

    }

    @SuppressWarnings("unchecked")
    public void registroComoLote(List<CampoActualizado> campos) {

        if (campos == null || campos.isEmpty()) {
            throw new AccesoDatosExcepcion("No es posible registrar sin datos");
        }

        int i = 0;
        Map<String, Object>[] params = new HashMap[campos.size()];
        for (CampoActualizado item : campos) {
            params[i] = mapaParametros(item);
            i++;
        }
        getJdbcTemplate().batchUpdate(getVarEntorno().getValor("registro.campoactualizado"), params);

    }

    private Map<String, Object> mapaParametros(CampoActualizado registro) {

        Map<String, Object> params = new HashMap<>(NUMERO_PARAMS_REGISTRO);
        params.put("dni", registro.getDni());
        params.put("periodo", registro.getNmperiodo());
        params.put("campo", registro.getCampo().toString());
        params.put("valorNuevo", registro.getValorNuevo());
        params.put("valorViejo", registro.getValorViejo());
        params.put("dniIngresa", registro.getDniIngresa());
        params.put("poliza", registro.getPoliza());
        params.put("tipoCotizante", registro.getTipoCotizante());
        params.put("tipoAfiliado", registro.getTipoAfiliado());

        return params;
    }

}
