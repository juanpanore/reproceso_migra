package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao.DatosCentroTrabajo;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao.DatosCobertura;
import com.sura.arl.reproceso.accesodatos.CentroTrabajoDao.RespuestaFechasCobertura;
import com.sura.arl.reproceso.util.UtilFechas;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class CentroTrabajoDaoUT {

    @InjectMocks
    CentroTrabajoDao sut;
    
    @Mock
    JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarDatosCT(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerDatosCentroTrabajo());
            
        
        DatosCentroTrabajo resultado = sut.consultarDatosCT("201012", "9008765433", "0120222");
        verify(varEntorno, times(1)).getValor("consulta.datosCT");
        assertTrue(Objects.nonNull(resultado));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarFechasCobertura(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1233221");
        afiliado.setTipoCotizante("1");
        afiliado.setCobertura(new Cobertura());
        afiliado.getCobertura().setPoliza("000");;
        
        afiliado.setTipoAfiliado("1");
        afiliado.setTipoCotizante("01");
        
        EstadoCuenta ec = EstadoCuenta.builder().afiliado(afiliado).build();
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerRespuestaFechasCobertura());
        
        RespuestaFechasCobertura resultado = sut.consultarFechasCobertura("201012", ec, afiliado);
        verify(varEntorno, times(1)).getValor("consulta.maxFechasCobertura");
        assertTrue(Objects.nonNull(resultado));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarConsultarDatosCobertura(){

        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(obtenerDatosCobertura());

        DatosCobertura resultado = sut.consultarDatosCobertura("201712", "0989767", "C1212122", "1","01");
        verify(varEntorno, times(1)).getValor("consulta.datosCobertura");
        assertTrue(Objects.nonNull(resultado));
    }
    
    @Test
    public void probarBuscarProximaFechaVCT(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class), eq(Date.class)))
        .thenReturn(UtilFechas.fechaActualSinHora());

        Date resultado = sut.buscarProximaFechaVCT("201712", "C1212122", "1");
        verify(varEntorno, times(1)).getValor("consulta.proximaFechaVCT");
        assertTrue(Objects.nonNull(resultado) && UtilFechas.fechaActualSinHora().equals(resultado));

    }
    
    private DatosCentroTrabajo obtenerDatosCentroTrabajo(){
        
        
        CentroTrabajoDao dao = new CentroTrabajoDao();
        DatosCentroTrabajo centroTrabajo = dao.new DatosCentroTrabajo();
        centroTrabajo.setFealta(UtilFechas.fechaActualSinHora());
        centroTrabajo.setFebaja(UtilFechas.fechaActualSinHora());
        centroTrabajo.setSucursalActualizar("01");
        centroTrabajo.setTasa(5.25);
        
        return centroTrabajo;
    }
    
    private RespuestaFechasCobertura obtenerRespuestaFechasCobertura(){
        
        CentroTrabajoDao dao = new CentroTrabajoDao();
        RespuestaFechasCobertura respuesta = dao.new RespuestaFechasCobertura();
        respuesta.setMaxFealta(UtilFechas.fechaActualSinHora());
        respuesta.setMaxFebaja(UtilFechas.fechaActualSinHora());
        respuesta.setTotalCoberturas(20);
        respuesta.setUltimoPeriodocotizado("201712");
       
        return respuesta;
    }
    
    private DatosCobertura obtenerDatosCobertura(){
        
        CentroTrabajoDao dao = new CentroTrabajoDao();
        DatosCobertura datosCobertura = dao.new DatosCobertura();
        datosCobertura.setCertificado(112233l);
        datosCobertura.setFebaja(UtilFechas.fechaActualSinHora());
        
        return datosCobertura;
    }
    

}
