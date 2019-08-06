package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.UltimosValoresCotizadosDao;
import com.sura.arl.reproceso.accesodatos.UltimosValoresCotizadosDao.RespuestaUltimoPeriodoCotizado;
import com.sura.arl.reproceso.accesodatos.UltimosValoresCotizadosDao.RespuestaUltimosValoresCotizados;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class UltimosValoresCotizadosDaoUT {
    
    @InjectMocks
    UltimosValoresCotizadosDao sut;
    
    @Mock
    VariablesEntorno varEntorno;

    @Mock
    JdbcCustomTemplate jdbcTemplateMock;
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarObtenerUltimoPeriodoCotizado(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerRespuestaUltimoPeriodoCotizado());    
        
        RespuestaUltimoPeriodoCotizado resultado = sut.obtenerUltimoPeriodoCotizado(TipoDocumento.NI, "1112222", "C2222111", "02","C");
        assertTrue(Objects.nonNull(resultado));
        verify(varEntorno, times(1)).getValor("consulta.ultimoPeriodoCotizado");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarObtenerUltimosValoresCotizados2388(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerRespuestaUltimosValoresCotizados());    

        RespuestaUltimosValoresCotizados resultado = sut.obtenerUltimosValoresCotizados("2388", "C111222", "02", TipoDocumento.NI, "12121212", "271012","C");
        assertTrue(Objects.nonNull(resultado));
        verify(varEntorno, times(1)).getValor("consulta.ultimosValoresCotizados.2388");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void probarObtenerUltimosValoresCotizados1747(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerRespuestaUltimosValoresCotizados());    

        RespuestaUltimosValoresCotizados resultado = sut.obtenerUltimosValoresCotizados("1747", "C111222", "02", TipoDocumento.NI, "12121212", "271012","C");
        assertTrue(Objects.nonNull(resultado));
        verify(varEntorno, times(1)).getValor("consulta.ultimosValoresCotizados.1747");
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarObtenerUltimosValoresCotizadosNoLaborado(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerRespuestaUltimosValoresCotizados());
        
        Optional<RespuestaUltimosValoresCotizados> resultado = sut.obtenerUltimosValoresCotizadosNoLaborado("1743", "C121232", "02", 
                                    TipoDocumento.NI, "434565544", "201712","C");  
        
        assertTrue(resultado.isPresent() && Objects.nonNull(resultado.get()));
        verify(varEntorno, times(1)).getValor("consulta.ultimosValoresCotizados.noLaborado");
    }
    
    
    @Test
    public void probarActualizar(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);
        sut.actualizar("C121212","02","0900002323", 1000l, 1200000l, "201712","01","01");
        verify(varEntorno, times(1)).getValor("actualizacion.ultimosValoresCotizados");
    }
    
    private RespuestaUltimosValoresCotizados obtenerRespuestaUltimosValoresCotizados(){
        
        UltimosValoresCotizadosDao dao = new UltimosValoresCotizadosDao();
        RespuestaUltimosValoresCotizados respuesta = dao.new RespuestaUltimosValoresCotizados();
        
        respuesta.setDias(10);
        respuesta.setIbc(1000l);
        respuesta.setSalario(1200000l);
        
        return respuesta;
    }
    
    private RespuestaUltimoPeriodoCotizado obtenerRespuestaUltimoPeriodoCotizado(){
        
        UltimosValoresCotizadosDao dao = new UltimosValoresCotizadosDao();
        RespuestaUltimoPeriodoCotizado respuesta = dao.new RespuestaUltimoPeriodoCotizado();
        respuesta.setLey("2388");
        respuesta.setPeriodo("201712");
        
        return respuesta;
    }
}
