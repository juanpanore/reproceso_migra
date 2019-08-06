package com.sura.arl.reproceso.accesodatos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class IngresoRetiroAfiliacionDao extends AbstractDAO {
    
    private static final String ING = "I";
    private static final String RET = "R";
    
    @Transactional
    public void registrar(Long numFormulario, Afiliado afiliado) {
        
        for (Map<String, Object> mapa : obtenerParametroAfiliado(numFormulario, afiliado)) {
            getJdbcTemplate().update(getVarEntorno().getValor("ingreso.ingresoretiro.afiliacion"), mapa);
        }
    }

    private List<Map<String,Object>> obtenerParametroAfiliado(Long numFormulario, Afiliado afiliado) {
        
        List<Map<String, Object>> listaParams = new ArrayList<>();
        
        Map<String, Object> param = new HashMap<>();
        param.put("numeroFormulario", numFormulario);
        param.put("dniEmpleado", afiliado.getDni());
        param.put("periodoCotizacion", afiliado.getCobertura().getPeriodo());
        param.put("poliza", afiliado.getCobertura().getPoliza());
        param.put("tipoCotizante", afiliado.getTipoCotizante());
        param.put("dniIngresa", getVarEntorno().getValor("usuario.dniingresa"));
        param.put("fechaInicioNovedad", afiliado.getFechaInicioNovedad());
        param.put("fechaFinNovedad", afiliado.getFechaFinNovedad());
        
        //Viene con novedad de ingreso
        if (afiliado.tieneNovedadIngreso()) {
            param.put("tipoNovedad", ING);
            listaParams.add(new HashMap<>(param));
        } 
        
        //Viene con novedad de retiro
        if (afiliado.tieneNovedadRetiro()) {
            param.put("tipoNovedad", RET);
            listaParams.add(new HashMap<>(param));
        }

        return listaParams;
    }   
}
