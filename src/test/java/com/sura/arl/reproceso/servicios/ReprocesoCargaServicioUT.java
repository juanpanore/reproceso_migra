package com.sura.arl.reproceso.servicios;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao;
import com.sura.arl.estadocuenta.accesodatos.TrazaEstadoCuentaDao;
import com.sura.arl.estadocuenta.servicios.ErroresProcesoServicio;
import com.sura.arl.reproceso.accesodatos.ControlNovedadesDao;
import com.sura.arl.reproceso.accesodatos.ControlReprocesoDao;
import com.sura.arl.reproceso.accesodatos.IngresoRetiroAfiliacionDao;
import com.sura.arl.reproceso.accesodatos.NovedadesDao;
import com.sura.arl.reproceso.modelo.DetallePago;
import com.sura.arl.reproceso.servicios.EnriquesServicio;
import com.sura.arl.reproceso.servicios.RenesServicio;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;

@RunWith(MockitoJUnitRunner.class)
public class ReprocesoCargaServicioUT {

    @Mock
    ControlReprocesoDao controlReprocesoDao;

    @Mock
    NovedadesDao novedadesDao;

    //@Mock
    //CalculoDiasIbcServicio calculoDiasIbcServicio;

    @Mock
    EstadoCuentaDao estadoCuentaDao;

    @Mock
    TrazaEstadoCuentaDao trazaEstadoCuentaDao;

    @Mock
    RenesServicio renesServicio;

    @Mock
    ErroresProcesoServicio erroresProcesoServicio;

    @Mock
    ControlNovedadesDao controlNovedadesDao;

    @Mock
    EnriquesServicio enriquesServicio;
    
    @Mock
    IngresoRetiroAfiliacionDao ingresoRetiroAfiliacionDao;

    @InjectMocks
    ReprocesoCargaServicio sut;

    @Test
    @Ignore
    public void probarReprocesoConRenesSinErrores() {

        // Inicializacion
        Long numFormulario = 1L;
        Integer totalRenes = 1;

        Cobertura cobertura = new Cobertura();
        cobertura.setPeriodo("201701");
        cobertura.setPoliza("94");

        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C11");
        afiliado.setCobertura(cobertura);

        List<Afiliado> listaAfiliados = new ArrayList<>();
        listaAfiliados.add(afiliado);
        
        List<DetallePago> pagos = new ArrayList<>();

        when(novedadesDao.obtenerAfiliadosXformulario(numFormulario,Optional.empty(),Optional.empty())).thenReturn(listaAfiliados);
        when(renesServicio.registrar(anyLong(), anyString(), anyString(), anyString())).thenReturn(totalRenes);
        doNothing().when(ingresoRetiroAfiliacionDao).registrar(anyLong(), any(Afiliado.class));
        
        // Ejecucion
        sut.ejecutar(numFormulario);

        // Verificacion
        verify(novedadesDao).actualizarProcesado(any(Afiliado.class), eq(true), pagos);
        verify(ingresoRetiroAfiliacionDao, never()).registrar(anyLong(), any(Afiliado.class));
        verify(renesServicio).registrar(anyLong(), anyString(), anyString(), anyString());
        verifyZeroInteractions(enriquesServicio);
        verify(controlNovedadesDao).actualizarProcesado(numFormulario, true);
    }

}
