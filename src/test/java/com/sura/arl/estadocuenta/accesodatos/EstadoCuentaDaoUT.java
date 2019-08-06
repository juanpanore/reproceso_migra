package com.sura.arl.estadocuenta.accesodatos;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.modelo.EstadoPago;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.modelo.excepciones.CambiosEsperadosExcepcion;
import com.sura.arl.reproceso.util.UtilFechas;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class EstadoCuentaDaoUT {
    
    @InjectMocks
    private EstadoCuentaDao sut;

    @Mock
    private JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;

    @Before
    public void inicializacion() throws Exception {
        sut = new EstadoCuentaDao();
        sut.setJdbcTemplate(jdbcTemplateMock);
        sut.setVarEntorno(varEntorno);
        Mockito.when(varEntorno.getValor(Mockito.anyString())).thenReturn("");
    }
    
    @Test
    public void probarRegistrar(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);
        sut.registrar(obtenerEstadoCuenta());
        verify(varEntorno).getValor("registro.esperada");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void probarRegistrarLista(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.batchUpdate(Mockito.anyString(), Mockito.any(Map[].class))).thenReturn(new int[] {1});
        sut.registrar(Arrays.asList(obtenerEstadoCuenta()));
        verify(varEntorno).getValor("registro.esperada");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void probarConsultarCoberturasXPeriodo(){
        
        when(varEntorno.getValor(anyString( ))).thenReturn("");
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(Arrays.asList(obtenerCoberturas()));
        
        List<Cobertura> resultado = sut.consultarCoberturasXPeriodo(UtilFechas.fechaActualSinHora(), 2);
        verify(varEntorno).getValor("consulta.estado.cuenta.coberturas");
        assertTrue(resultado.size() > 0);
    }
    
   @Ignore
    @Test
    public void probarActualizar(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        sut.actualizar(obtenerEstadoCuenta());
        verify(varEntorno).getValor("actualizar.item.estado.cuenta");
    }
    
    @Test(expected=CambiosEsperadosExcepcion.class)
    public void probarActualizarFallida(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(0);

        sut.actualizar(obtenerEstadoCuenta());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void probarConsultarXafiliadoPeriodo(){
            
        when(varEntorno.getValor(anyString( ))).thenReturn("");
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(obtenerEstadoCuenta());
        
        EstadoCuenta resultado = sut.consultarXafiliadoPeriodo(obtenerEstadoCuenta().getAfiliado(), "201712");
        
        assertTrue(!Objects.isNull(resultado));
        verify(varEntorno).getValor("consulta.estado.cuenta.xAfiliadoPeriodo");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void probarConsultarXafiliadoDniPeriodo(){
        
        when(varEntorno.getValor(anyString( ))).thenReturn("");
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(obtenerEstadoCuenta());
        
        EstadoCuenta resultado = sut.consultarXafiliadoPeriodo("C1","201712");
        
        assertTrue(!Objects.isNull(resultado));
        verify(varEntorno).getValor("consulta.estado.cuenta.xAfiliadoPeriodo");
    }
    
    @Test
    public void probarActualizarXcambioDeCT(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        sut.actualizarXcambioDeCT(obtenerEstadoCuenta(), "201712");
        verify(varEntorno).getValor("actualizar.estadoCuenta_CT");
    }
    
    @Test(expected=CambiosEsperadosExcepcion.class)
    public void probarActualizarXcambioDeCTFallida(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(0);
        sut.actualizarXcambioDeCT(obtenerEstadoCuenta(), "201712");
    }
    
    @Test
    public void probarMarcarPagoDeAfiliados(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        sut.marcarPagoDeAfiliados(obtenerEstadoCuenta().getAfiliado().getCobertura(), UtilFechas.fechaActualSinHora());
        verify(varEntorno).getValor("marcar.pago.estado.cuenta.afiliados");
    }
    
    @Test
    public void probarMarcarPagoDeAfiliado(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        sut.marcarPagoDeAfiliado(1l, obtenerEstadoCuenta().getAfiliado());
        verify(varEntorno).getValor("marcar.pago.estado.cuenta.afiliado");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void probarObtenerSumatoriaIbcPorIndependiente(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(Arrays.asList(obtenerEstadoCuenta().getAfiliado()));
        
        List<Afiliado> resultado = sut.obtenerSumatoriaIbcPorIndependiente("201710", "90001, 90002, 90003",25);
        assertTrue(resultado.size() > 0);
    }
    
    @Test
    public void probarMarcarPagoIndependiente(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        sut.marcarPagoIndependiente(obtenerEstadoCuenta().getAfiliado());
        verify(varEntorno).getValor("marcar.pago.estadoCuenta.independiente");
    }
    
    @Test
    public void probarMarcarPagoIndependienteLista(){
        
        when(varEntorno.getValor(anyString())).thenReturn("");
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        sut.marcarPagoIndependiente(Arrays.asList(obtenerEstadoCuenta().getAfiliado()));
        verify(varEntorno).getValor("marcar.pago.estadoCuenta.independiente");
    }
    
    private EstadoCuenta obtenerEstadoCuenta(){
        
        Cobertura cobertura = new Cobertura();
        cobertura.setPoliza("90002312334");
        cobertura.setPeriodoGeneracion("201711");
        cobertura.setPeriodo("201711");
        
        Condicion condicion = new Condicion();
        condicion.setTipoGeneracion("A");
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1111");
        afiliado.setCobertura(cobertura);
        afiliado.setCondicion(condicion);
        
        return EstadoCuenta.builder().centroTrabajo("C1").dias(30).cotizacion(1000000l)
                            .afiliado(afiliado).estadoPago(EstadoPago.AFILIADO_OK).build();
    }
    
    private List<Cobertura> obtenerCoberturas(){
        
        Cobertura cobertura = new Cobertura();
        cobertura.setPoliza("90002312334");
        cobertura.setPeriodoGeneracion("201711");
        cobertura.setPeriodo("201711");
        
        return Arrays.asList(cobertura);        
    }
   
}
    