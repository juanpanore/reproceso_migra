package com.sura.arl.reproceso.servicios;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.accesodatos.IbcCotizacionDao;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.reproceso.servicios.EnriquesServicio;

@RunWith(MockitoJUnitRunner.class)
public class EnriquesServicioUT {

    @Mock
    EstadoCuentaServicio servicio;

    @Mock
    IbcCotizacionDao ibcCotizacionDao;

    @InjectMocks
    EnriquesServicio sut;

    @Test
    public void validarProcesoInconsistenciaNoPagado() {
        Afiliado afiliado = new Afiliado();
        afiliado.setDni("C1");
        afiliado.setTipoCotizante("01");
        Cobertura cobertura = new Cobertura();
        cobertura.setPeriodo("201705");
        cobertura.setPoliza("094341575");
        afiliado.setCobertura(cobertura);
        Long numFormulario = 0L;

        sut.validarProcesoInconsistenciaNoPagado(numFormulario, afiliado);
        verify(servicio).marcarPagoDeAfiliado(numFormulario, afiliado);
    }

    @Test
    public void iniciarProcesoInconsistenciasNoPagados() {
        sut.iniciarProcesoInconsistenciasNoPagados();
    }

    @Test
    public void probarMarcacionIndependienteMas25SalarosMinimos() {

        Cobertura cobertura = new Cobertura();
        cobertura.setPeriodo("201705");
        cobertura.setPoliza("094341575");

        Afiliado afiliado = new Afiliado();
        afiliado.setUltimoIbc(20000000d);

        Mockito.when(servicio.consultarCoberturas(Mockito.any(Date.class))).thenReturn(Arrays.asList(cobertura));
        Mockito.doNothing().when(servicio).marcarPagoDeAfiliados(Mockito.any(Cobertura.class), Mockito.any(Date.class));
        Mockito.when(ibcCotizacionDao.consultarSalarioMinimoXperiodo(Mockito.anyString())).thenReturn(781242d);
        Mockito.when(servicio.obtenerSumatoriaIbcPorIndependiente(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt())).thenReturn(Arrays.asList(afiliado));

        
        sut.iniciarProcesoInconsistenciasNoPagados();

        Mockito.verify(servicio, times(1)).marcarPagoIndependiente(Mockito.anyListOf(Afiliado.class));
    }

}
