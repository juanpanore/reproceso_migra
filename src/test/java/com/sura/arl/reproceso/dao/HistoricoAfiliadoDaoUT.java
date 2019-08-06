package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.HistoricoAfiliadosDao;
import com.sura.arl.reproceso.modelo.HistoricoAfiliado;
import com.sura.arl.reproceso.util.UtilFechas;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class HistoricoAfiliadoDaoUT {
    
    @InjectMocks
    HistoricoAfiliadosDao sut;
    
    @Mock
    JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarRegistroAfectado(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class),any(RowMapper.class)))
        .thenReturn(obtenerHistoricoAfiliado());
        
        HistoricoAfiliado resultado = sut.consultarRegistroAfectado("900001", "C11233", 10l, "201712");
        
        assertTrue(Objects.nonNull(resultado));
        verify(varEntorno, times(1)).getValor("consulta.historicoAfiliado.afectado");
    }
    
    @Test
    public void probarActualizarFebaja(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);  
        sut.actualizarFebaja(obtenerHistoricoAfiliado(), UtilFechas.fechaActualSinHora());
        Mockito.verify(varEntorno, times(1)).getValor("actualizar.febaja.historicoAfiliado.afectado");
    }
    
    @Test
    public void probarInsertar(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);  
        sut.insertar(obtenerHistoricoAfiliado());
        Mockito.verify(varEntorno, times(1)).getValor("insertar.historicoAfiliado.afectado");
    }
    
    
    private HistoricoAfiliado obtenerHistoricoAfiliado(){
        
        HistoricoAfiliado historico = new HistoricoAfiliado();
        historico.setPoliza("900878");
        historico.setDni("C11233");
        historico.setSucursal("0000001");
        historico.setFealta(UtilFechas.fechaActualSinHora());
        historico.setFebaja(UtilFechas.fechaActualSinHora());
        historico.setCertificado(100l);
        historico.setFuente("01");
        historico.setDniIngresa("C1111");
        historico.setSucursalPagadora("0000002");
        historico.setTema("tema1");
        
        return historico;
    }
}
