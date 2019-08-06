package com.sura.arl.reproceso.dao;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.RenesDao;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)

public class RenesDaoUT {

    @InjectMocks
    RenesDao sut;
    
    @Mock
    JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;
    
    @Test
    public void probarRegistrar(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);  
        sut.registrar(100l, "C121212", "01", "01");
        Mockito.verify(varEntorno, times(1)).getValor("ingresar.renes.segun.legalizacion");
        
    }
    
}
