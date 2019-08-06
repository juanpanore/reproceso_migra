package com.sura.arl.reproceso.servicios.integrador;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.Assert;

import com.sura.arl.afiliados.accesodatos.AfiliadosCoberturaDao;
import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.integrador.accesodatos.CambiosEstadoCuentaDao;
import com.sura.arl.reproceso.accesodatos.ControlNovedadesDao;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;
import com.sura.arl.reproceso.servicios.RenesServicio;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;
import com.sura.arl.reproceso.servicios.generales.ReprocesoAfiliadoServicio;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

/**
 *
 * @author pragma.co
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegradorEsperadaAfiliadoServicioUT {

    @InjectMocks
    IntegradorEsperadaAfiliadoServicio integradorEsperadaAfiliadoServicio;

    @Mock
    AfiliadosCoberturaDao afiliadosCoberturaDao;
    
    @Mock
    CambiosEstadoCuentaDao cambiosEstadoCuentaDao;

    @Mock
    ControlNovedadesDao controlNovedadesDao;

    @Mock
    ReprocesoCargaServicio reprocesoCargaServicio;

    @Mock
    RenesServicio renesServicio;

    @Mock
    ReprocesoAfiliadoServicio reprocesoAfiliadoServicio;

    @Mock
    EstadoCuentaServicio esperadaServicio;

    @Spy
    ActorSystem actorSystem;

    @Spy
    Materializer materializer;
    
    @Before
    public void before() {
        actorSystem = ActorSystem.create("procesador-reproceso-test", ConfigFactory.empty());
        materializer = ActorMaterializer.create(actorSystem);
    }

    @Test
    public void registrarAfiliadoTest() {

        IntegradorEsperadaAfiliado integradorEsperada = new IntegradorEsperadaAfiliado();
        integradorEsperada.setPoliza("094");
        integradorEsperada.setDniAfiliado("1234");
        integradorEsperada.setCertificado("1");
        integradorEsperada.setTipoAfiliado("01");
        integradorEsperada.setTipoCotizante("59");

        Assert.notNull(integradorEsperadaAfiliadoServicio);

        Afiliado a = new Afiliado();
        a.setDni("1234");
        //a.setFechaAlta(Date.from(LocalDate.of(2018, Month.JANUARY, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        a.setCondicion(new Condicion());
        a.getCondicion().setTipoGeneracion("v");
        a.setCobertura(new Cobertura("094"));
        a.getCobertura().setFealta(new Date());
        a.getCobertura().setFebaja(new Date());
        

        Mockito.when(afiliadosCoberturaDao.consultarAfiliado(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(a);
        
        Mockito.when(esperadaServicio.calcularEstadoCuenta(Mockito.any(Afiliado.class))).thenReturn(EstadoCuenta.builder().afiliado(a).build());
        
        try {
            integradorEsperadaAfiliadoServicio.registrarAfiliado(integradorEsperada);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Mockito.verify(afiliadosCoberturaDao).consultarAfiliado("094", "1234", "01", "59", "1");

    }

}
