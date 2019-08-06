package com.sura.arl.integrador.accesodatos;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.integrador.modelo.ControlIntegrador;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author pragma.co
 */
@Repository
public class ControlIntegradorDao extends AbstractDAO{
    
    @Transactional
    public Integer guardar(ControlIntegrador controlIntegrador){
        
        Map<String, Object> params = new HashMap<>();
        params.put("periodo", controlIntegrador.getPeriodo().format("yyyyMM"));
        params.put("contrato", controlIntegrador.getContrato());
        params.put("cambio", controlIntegrador.getMotivoCambio().name());
        params.put("dniIngreso", controlIntegrador.getDniIngreso());
        
        return getJdbcTemplate().update(getVarEntorno().getValor("control.integrador.insert"), params);
    }
    
     @Transactional
    public int[] guardar(List<ControlIntegrador> controlIntegrador){
        
        Map<String, Object>[] params = new HashMap[controlIntegrador.size()];
        for(int i=0; i<params.length; i++){
            params[i] = new HashMap<>();
            params[i].put("periodo", controlIntegrador.get(i).getPeriodo().format("yyyyMM"));
            params[i].put("contrato", controlIntegrador.get(i).getContrato());
            params[i].put("cambio", controlIntegrador.get(i).getMotivoCambio().name());
            params[i].put("feIngreso", controlIntegrador.get(i).getFechaIngreso());
            params[i].put("dniIngreso", controlIntegrador.get(i).getDniIngreso());
        }
                       
        return getJdbcTemplate().batchUpdate(getVarEntorno().getValor("control.integrador.insert"), params); 
    }
    
}
