package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.ControlReprocesoDao;
import com.sura.arl.reproceso.accesodatos.ControlReprocesoDao.EstadoReproceso;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class ControlReprocesoDaoUT {
    
    @InjectMocks
    ControlReprocesoDao sut;
    @Mock
    JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarFormulariosPendientesReproceso(){
        
        Mockito.when(jdbcTemplateMock.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Arrays.asList(1l));
    
        List<Long> resultado =  sut.consultarFormulariosPendientesReproceso();
        Mockito.verify(varEntorno, times(1)).getValor("buscar.formularios.pendientes.reproceso");
        
        assertTrue(resultado.size() > 0);
    }
    
    @Test
    public void probarRegistrar(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);  
        sut.registrar(1000l, EstadoReproceso.PROCESADO);
        Mockito.verify(varEntorno, times(1)).getValor("ingresar.registro.controlReproceso");
    }
    
}
