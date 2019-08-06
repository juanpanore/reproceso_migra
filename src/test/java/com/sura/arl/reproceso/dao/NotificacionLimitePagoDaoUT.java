package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.general.accesodatos.QueriesDAO;
import com.sura.arl.reproceso.accesodatos.NotificacionLimitePagoDao;
import com.sura.arl.reproceso.modelo.notificacion.EstadoNotificacionLimitePago;
import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.util.UtilFechas;
import com.sura.arl.reproceso.util.ValidacionEmail;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class NotificacionLimitePagoDaoUT {

    @InjectMocks
    NotificacionLimitePagoDao sut;
    
    @Mock
    JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;
    
    @Mock
    ValidacionEmail validacionEmail;
    
    @Mock
    QueriesDAO queriesDAO;
    
    @Before
    public void inicializacion() throws Exception {
        sut.setJdbcTemplate(jdbcTemplateMock);
        sut.setVarEntorno(varEntorno);
        when(varEntorno.getValor(anyString())).thenReturn("");
    }
    
    @Test
    public void probarConsultaFechaLimitePago(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class),eq(Date.class)))
        .thenReturn(UtilFechas.fechaActualSinHora());
        
        Optional<Date> resultado = sut.consultaFechaLimitePago("900011", "201712");
        
        assertTrue(resultado.isPresent() && resultado.get().compareTo(UtilFechas.fechaActualSinHora()) == 0);
        verify(varEntorno, times(1)).getValor("obtener.fechaLimitePago.polizaXPeriodo");
    }
    
    @Test
    public void probarSiExisteFechaFestiva(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class),eq(Date.class)))
        .thenReturn(UtilFechas.fechaActualSinHora());
        
        boolean resultado = sut.existeFechaFestiva(UtilFechas.fechaActualSinHora());
        
        assertTrue(resultado);
        verify(varEntorno, times(1)).getValor("consulta.validacion.diafestivo");
    }
    
    @Test
    public void probarNoExisteFechaFestiva(){
        
        when(jdbcTemplateMock.queryForObject(anyString(), anyMapOf(String.class, Object.class),eq(Date.class)))
        .thenThrow(new EmptyResultDataAccessException(1));

        
        boolean resultado = sut.existeFechaFestiva(UtilFechas.fechaActualSinHora());
        
        assertTrue(!resultado);
        verify(varEntorno, times(1)).getValor("consulta.validacion.diafestivo");
    }
    
    @Test
    public void probarIngresar(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);
        sut.ingresar(obtenerNotificacionLimtePago());
        
        verify(varEntorno, times(1)).getValor("ingreso.notificacion.limitePago");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarIngresarLista(){
        
        when(jdbcTemplateMock.batchUpdate(Mockito.anyString(), Mockito.any(Map[].class))).thenReturn(new int[] {1});
        sut.ingresar(Arrays.asList(obtenerNotificacionLimtePago()));
        
        verify(varEntorno, times(1)).getValor("ingreso.notificacion.limitePago");
    }
    
    
    @Test
    public void probarActualizar(){
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);
        sut.actualizar(obtenerNotificacionLimtePago());
        verify(varEntorno, times(1)).getValor("actualizacion.notificacion.limitePago");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void probarActualizarLista() {

        when(jdbcTemplateMock.batchUpdate(Mockito.anyString(), Mockito.any(Map[].class))).thenReturn(new int[] {1});
        sut.actualizar(obtenerNotificacionLimtePago());
        verify(varEntorno, times(1)).getValor("actualizacion.notificacion.limitePago");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void probarObtenerContratosPeriodoVencidoANotificarPorFecha(){
        
        when(queriesDAO.getQuery(anyString())).thenReturn("");
        
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
        .thenReturn(Arrays.asList(obtenerNotificacionLimtePago()));
        
        List<NotificacionLimitePago> resultado = sut.obtenerContratosPeriodoVencidoANotificarPorFecha(UtilFechas.fechaActualSinHora(), "201712");
        
        assertTrue(resultado.size() > 0);
    }
    
    public NotificacionLimitePago obtenerNotificacionLimtePago(){
        
        NotificacionLimitePago notificacion = new NotificacionLimitePago();
        notificacion.setPoliza("9000344");
        notificacion.setEstadoNotificacion(EstadoNotificacionLimitePago.NOTIFICADO);
        notificacion.setPeriodo("201712");
        notificacion.setCorreoAfiliado(Optional.of("prueba@prueba.com.co"));
        
        
        return notificacion;
    }

    
    
}
