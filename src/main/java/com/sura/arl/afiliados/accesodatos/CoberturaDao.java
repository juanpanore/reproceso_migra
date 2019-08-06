package com.sura.arl.afiliados.accesodatos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.general.accesodatos.QueriesDAO;

/*
 * Pendiente de organizar para que quedo solo un componente
 */
public class CoberturaDao extends AbstractDAO {
    
    @Autowired
    QueriesDAO queriesDAO;
    
    public List<Cobertura> consultarCoberturasNotificacionEmpresa(String periodo) {
        
        
        String sql = queriesDAO.getQuery("consulta.coberturas.notificacionEmpresas");
        //String sql = getVarEntorno().getValor("obtener.coberturas.notificacion.empresas");
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        
        return getJdbcTemplate().query(sql, params,
                (ResultSet rs, int index) -> {
                    Cobertura cobertura = new Cobertura(rs.getString("NPOLIZA"),periodo );
                    return cobertura;
                });
    }
    
    public List<Cobertura> consultarCoberturasNotificacionVoluntarios(String periodo) {
        
        String sql = queriesDAO.getQuery("consulta.coberturas.notificacionVoluntarios"); 
        //String sql = getVarEntorno().getValor("obtener.coberturas.notificacion.voluntarios");
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);

        return getJdbcTemplate().query(sql, params,
                (ResultSet rs, int index) -> {
                    Cobertura cobertura = new Cobertura(rs.getString("NPOLIZA"),periodo );
                    return cobertura;
                });
    }
}
