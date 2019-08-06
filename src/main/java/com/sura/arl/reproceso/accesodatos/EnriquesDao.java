package com.sura.arl.reproceso.accesodatos;

import com.sura.arl.general.accesodatos.AbstractDAO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 *
 * @author pragma.co
 */
@Repository
public class EnriquesDao extends AbstractDAO{
    
    
    public Integer marcarEnrique(String poliza, String periodo, String tipoCotizante, String tipoAfiliado, String afiliado, String modifica){
        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("periodo", periodo);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("dniAfiliado", afiliado);
        params.put("dniModifica", modifica);


        return getJdbcTemplate().update(getVarEntorno().getValor("actualizar.enriques"), params);
    }
    
    public Boolean existe(String poliza, String dniEmpleado, String periodo, String tipoAfiliado, String tipoCotizante) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("periodo", periodo);
        params.put("dniAfiliado", dniEmpleado);
        params.put("tipoAfiliado", tipoAfiliado);

        List<Integer> result = getJdbcTemplate().query(getVarEntorno().getValor("existen.pagos"), params,(rs, i) -> {
            return rs.getInt("PAGOS");
        });

        return !result.isEmpty();
    }
    
}
