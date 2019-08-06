package com.sura.arl.reproceso.dao;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.IngresoRetiroAfiliacionDao;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)

public class IngresoRetiroAfiliacionDaoUT {

    @InjectMocks
    IngresoRetiroAfiliacionDao sut;

    @Mock
    VariablesEntorno entorno;

    @Mock
    JdbcCustomTemplate jdbcTemplate;
    
    @Test
    public void testRegistrar(){
        
       when(jdbcTemplate.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);       
       sut.registrar(11111l, obtenerAfiliado());
       verify(entorno, times(2)).getValor("ingreso.ingresoretiro.afiliacion");
    }
    
    private Afiliado obtenerAfiliado(){
        
        Afiliado afiliado = new Afiliado();
        
        Cobertura cobertura = new Cobertura();
        cobertura.setPeriodo("201802");
        cobertura.setPoliza("0099898922");
        
        afiliado.setCobertura(cobertura);
        afiliado.setDni("C11111");
        afiliado.setPeriodoCotizacion("201802");
        afiliado.setTieneNovedadIngreso(true);
        afiliado.setTieneNovedadRetiro(true);

        return afiliado;
    }
}
