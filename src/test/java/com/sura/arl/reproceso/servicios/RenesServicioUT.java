package com.sura.arl.reproceso.servicios;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.reproceso.accesodatos.RenesDao;
import com.sura.arl.reproceso.servicios.RenesServicio;

@RunWith(MockitoJUnitRunner.class)
public class RenesServicioUT {

    @Mock
    RenesDao renesDao;

    @InjectMocks
    RenesServicio sut;

    //@Test
    public void probarRegistroSegunLegalizacion() {
        Long numeroFormulario = 1L;
        String dniAfiliado = "C1";
        String tipoCotizante = "01";
        String tipoAfiliado = "01";
        sut.registrar(numeroFormulario, dniAfiliado, tipoCotizante, tipoAfiliado);
        verify(renesDao).registrar(numeroFormulario, dniAfiliado, tipoCotizante ,tipoAfiliado);
    }

    @Test(expected = NullPointerException.class)
    public void probarRegistroConNumeroFormularioNulo() {
        Long numeroFormulario = null;
        String dniAfiliado = "C1";
        String tipoAfiliado = "01";
        String tipoCotizante = "01";
        sut.registrar(numeroFormulario, dniAfiliado, tipoCotizante, tipoAfiliado);
        verify(renesDao, Mockito.never()).registrar(numeroFormulario, dniAfiliado, tipoCotizante, tipoAfiliado);
    }
}
