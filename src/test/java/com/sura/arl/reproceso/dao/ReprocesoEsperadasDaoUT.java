package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.ReprocesoEstadoCuentaDao;
import com.sura.arl.reproceso.modelo.DatosNovedades;
import com.sura.arl.reproceso.util.UtilFechas;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class ReprocesoEsperadasDaoUT {
    
    @InjectMocks
    ReprocesoEstadoCuentaDao sut;
    
    @Mock
    VariablesEntorno varEntorno;

    @Mock
    JdbcCustomTemplate jdbcTemplateMock;
    
    @Before
    public void inicializacion() throws Exception {
        sut.setJdbcTemplate(jdbcTemplateMock);
        sut.setVarEntorno(varEntorno);

        Mockito.when(varEntorno.getValor(Mockito.anyString())).thenReturn("");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarObtenerLeyNovedades(){
        
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(Arrays.asList("2388"));
        
        List<String> resultado = sut.obtenerLeyNovedades("201712", "45433331,344333333");
        assertEquals(1, resultado.size());        
        Mockito.verify(varEntorno, times(1)).getValor("consultar.ley.proceso");
    }
    
    @Test
    public void probarObtenerContratosIndependientes(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1233221");
        afiliado.setTipoCotizante("1");
        afiliado.setTipoDocumentoEmpleador(TipoDocumento.CC);
        
        Mockito.when(jdbcTemplateMock.queryForObject(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                eq(String.class))).thenReturn("010101,020202");
        
        String resultado = sut.obtenerContratosIndependientes("201712", "0000001, 0000002", afiliado);
        verify(varEntorno, times(1)).getValor("consultar.contratos.independientes");
        assertTrue(Objects.nonNull(resultado));
    }
    
    @Test
    public void probarObtenerContratosXAfiliado(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1233221");
        afiliado.setTipoCotizante("1");
        afiliado.setTipoDocumentoEmpleador(TipoDocumento.CC);
        
        Mockito.when(jdbcTemplateMock.queryForObject(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                eq(String.class))).thenReturn("010101,020202");
        
        String resultado = sut.obtenerContratosXAfiliado("201712", afiliado);
        verify(varEntorno, times(1)).getValor("consultar.contratos.afiliado");
        assertTrue(Objects.nonNull(resultado));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarSinNovedades1747(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1233221");
        afiliado.setTipoCotizante("1");
        afiliado.setTipoDocumentoEmpleador(TipoDocumento.CC);
        
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerDatosNovedades());
        
        List<DatosNovedades> resultado = sut.consultarSinNovedades1747("201712", afiliado, "00001,00002", "00003,00004");
        verify(varEntorno, times(1)).getValor("consultar.sin.novedades.1747");
        assertEquals(1, resultado.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarConNovedades(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1233221");
        afiliado.setTipoCotizante("1");
        afiliado.setTipoDocumentoEmpleador(TipoDocumento.CC);
        
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerDatosNovedades());
        
        List<DatosNovedades> resultado = sut.consultarConNovedades("201712", afiliado, "00001,00002");
        verify(varEntorno, times(1)).getValor("consultar.con.novedades.N");
        assertEquals(1, resultado.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarSinNovedades(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1233221");
        afiliado.setTipoCotizante("1");
        afiliado.setTipoDocumentoEmpleador(TipoDocumento.CC);
        
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerDatosNovedades());
        
        List<DatosNovedades> resultado = sut.consultarSinNovedades("201712", afiliado, "00001,00002");
        verify(varEntorno, times(1)).getValor("consultar.sin.novedades.N");
        assertEquals(1, resultado.size());
    }
    
    private List<DatosNovedades> obtenerDatosNovedades(){
        
        //ReprocesoEsperadasDao dao = new ReprocesoEsperadasDao(null);
        DatosNovedades datosNovedades =  new DatosNovedades();
        datosNovedades.setCotizacion(1000l);
        datosNovedades.setDias(10d);
        datosNovedades.setFechaPago(UtilFechas.fechaActualSinHora());
        datosNovedades.setIbc(1000000d);
        
        return Arrays.asList(datosNovedades);
    }
    
    
}
