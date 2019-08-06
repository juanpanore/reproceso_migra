package com.sura.arl.reproceso.dao;


import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.accesodatos.CoberturaDao;
import com.sura.arl.reproceso.accesodatos.NovedadesDao;
import com.sura.arl.reproceso.modelo.DetallePago;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.TipoPlanilla;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class NovedadesDaoUT {

    @InjectMocks
    NovedadesDao sut;

    @Mock
    JdbcCustomTemplate jdbcTemplateMock;

    @Mock
    VariablesEntorno varEntorno;
    
    @Mock
    CoberturaDao coberturaDao;
    
    @Before
    public void inicializacion() throws Exception {
        sut.setJdbcTemplate(jdbcTemplateMock);
        sut.setVarEntorno(varEntorno);

        Mockito.when(varEntorno.getValor(Mockito.anyString())).thenReturn("");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void getAfiliadosXformularioTest() {

        Long numero = 1L;

        Mockito.when(jdbcTemplateMock.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Collections.emptyList());

        sut.obtenerAfiliadosXformulario(numero,Optional.empty(),Optional.empty());

        Mockito.verify(varEntorno, times(1)).getValor("consulta.afiliadosXformulario");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void probarObtenerAfiliadosXformulario(){
              
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(Arrays.asList(obtenerAfiliado()));
        
        List<Afiliado> respuesta = sut.obtenerAfiliadosXformulario(0001l,Optional.empty(),Optional.empty());
        verify(varEntorno).getValor("consulta.afiliadosXformulario");
        assertTrue(!respuesta.isEmpty());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void probarObtenerNovedadIngresoRetiroAfiliadosXformulario(){
              
        when(jdbcTemplateMock.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(Arrays.asList(obtenerAfiliado()));
        
        List<Afiliado> respuesta = sut.obtenerNovedadIngresoRetiroAfiliadosXformulario(0001l);
        verify(varEntorno).getValor("consulta.novedades.ingreso.retiro.afiliacion");
        assertTrue(!respuesta.isEmpty());
    }

    @Test
    public void probarActualizarProcesado(){
        
        when(jdbcTemplateMock.update(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);

        DetallePago d = new DetallePago("", "", new Date(), TipoPlanilla.A, "", 1L, "", "01", "", "");
        List<DetallePago> pagos = new ArrayList<>();
        pagos.add(d);
        sut.actualizarProcesado(obtenerAfiliado(),false,pagos);
        verify(varEntorno).getValor("actualizar.estadoCarga.afiliado.novedades");
    }
    
    
    private Afiliado obtenerAfiliado(){
        
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C112233");
        afiliado.setCertificado("CER111");
        
        Cobertura cobertura = new Cobertura();
        cobertura.setPeriodo("201712");
        afiliado.setCobertura(cobertura);
        
        return afiliado;
    }
    
    private InfoNovedadVCT obtenerResumenNovedad(){
        
        InfoNovedadVCT resumen = new InfoNovedadVCT();
        resumen.setCentroTrabajo("C1");
        //resumen.setCotizacion(98l);
        //resumen.setLey("2388");
        
        return resumen;
    }
    
}
