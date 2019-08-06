package com.sura.arl.reproceso.servicios.generales;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.afiliados.accesodatos.AfiliadosCoberturaDao;
import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;
import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.estadocuenta.accesodatos.DiasEsperadosDao;
import com.sura.arl.estadocuenta.accesodatos.IbcCotizacionDao;
import com.sura.arl.estadocuenta.accesodatos.TasaEsperadaDao;
import com.sura.arl.estadocuenta.actores.EstadoCuentaActor;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.reproceso.accesodatos.ReprocesoEstadoCuentaDao;
import com.sura.arl.reproceso.modelo.ConsolidadoNovedades;
import com.sura.arl.reproceso.modelo.DatosNovedades;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.ResultadoRecalculo;
import com.sura.arl.reproceso.modelo.TipoPlanilla;
import com.sura.arl.reproceso.modelo.excepciones.ReprocesoAfiliadoCanceladoExcepcion;

@RunWith(MockitoJUnitRunner.class)
public class RecalculoEstadoCuentaServicioUT {
    // @Autowired
    // ApplicationContext context;

    @Mock
    ReprocesoEstadoCuentaDao reprocesoEsperadasDao;

    @Mock
    ActualizacionCentroTrabajoServicio actualizacionCT;

    @Mock
    IbcCotizacionDao ibcCotizacionDao;

    @Mock
    DiasEsperadosDao diasEsperadosDao;
    
    @Mock
    EstadoCuentaServicio estadoCuentaServicio;
    
    @Mock
    TasaEsperadaDao tasaEsperadaDao;
    
    @Mock
    EstadoCuentaActor estadoCuentaActor;
    
    @Mock
    AfiliadosCoberturaDao afiliadosCoberturaDao;

    @InjectMocks
    RecalculoEstadoCuentaServicio sut;

    InfoNovedadVCT resumenNovedad;
    EstadoCuenta.Builder estadoCuentaActual;
    Afiliado afiliado;
    List<DatosNovedades> novedadesAusentismo;
    List<DatosNovedades> novedadesLaboradas;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    Double salarioMinimo2017 = 737717D;
    String periodo = "072017";
    Long numeroFormulario = 1111111111L;
    ConsolidadoNovedades consolidado = new ConsolidadoNovedades();

    @Before
    public void inicializacion() throws Exception {

        sut = new RecalculoEstadoCuentaServicio(reprocesoEsperadasDao, ibcCotizacionDao, diasEsperadosDao,
                actualizacionCT, estadoCuentaServicio, afiliadosCoberturaDao);
        resumenNovedad = new InfoNovedadVCT();
        resumenNovedad.setCentroTrabajo("0000000001");
        resumenNovedad.setTotalNovedadesVct(0D);

        Cobertura cb = new Cobertura();
        cb.setPoliza("00000001");
        Condicion cn = new Condicion();
        cn.setIbcMinimo(1);
        cn.setIbcMaximo(25);
        afiliado = new Afiliado();
        afiliado.setCertificado("01");
        afiliado.setCobertura(cb);
        afiliado.setCondicion(cn);
        afiliado.setDni("2222222222");
        afiliado.setDniEmpleador("11111111111");
        afiliado.setTipoDocumentoEmpleador(TipoDocumento.NI);
        afiliado.setDniEmpleador("11111111");
        afiliado.setTipoAfiliado("01");

        estadoCuentaActual = EstadoCuenta.builder().afiliado(afiliado);

        novedadesAusentismo = new ArrayList<>();
        novedadesLaboradas = new ArrayList<>();

        consolidado.setFormulariosAfectados(new ArrayList<Long>());
        consolidado.getFormulariosAfectados().add(1L);
        consolidado.setLey("1747");

        Mockito.doReturn(salarioMinimo2017).when(ibcCotizacionDao).consultarSalarioMinimoXperiodo(Mockito.anyString());
    }

    @Test
    public void pruebaGenerica() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        // ausentismo
        DatosNovedades a = new DatosNovedades();
        /*a.setCotizacion(0L);
        a.setDias(6D);
        a.setFechaPago(formatter.parse("2017-08-08"));
        a.setIbc(666334D);
        a.setIngreso(false);
        a.setRetiro(false);
        a.setTasa(0.0);
        a.setTipoPlanilla(TipoPlanilla.E);
        a.setNumeroFormulario(numeroFormulario);
        novedadesAusentismo.add(a);*/

        // laboradas
        DatosNovedades l = new DatosNovedades();
        l.setCotizacion(36100L);
        l.setDias(30D);
        l.setFechaPago(formatter.parse("2017-08-08"));
        l.setIbc(828116D);
        l.setIngreso(false);
        l.setRetiro(false);
        l.setTasa(4.35);
        l.setTipoPlanilla(TipoPlanilla.$);
        l.setNumeroFormulario(numeroFormulario);

        novedadesLaboradas.add(l);

        /*DatosNovedades l2 = new DatosNovedades();
        l2.setCotizacion(5600L);
        l2.setDias(24D);
        l2.setFechaPago(formatter.parse("2017-10-01"));
        l2.setIbc(1061984D);
        l2.setIngreso(true);
        l2.setRetiro(false);
        l2.setTasa(0.522);
        l2.setTipoPlanilla(TipoPlanilla.E);
        l2.setNumeroFormulario(numeroFormulario);*/
        // novedadesLaboradas.add(l2);

        // esperada
        EstadoCuenta ec = estadoCuentaActual.cotizacion(34000L).dias(30).ibc(828116L).tasa(4.35).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        consolidado.setAusentismoOriginal(novedadesAusentismo);
        consolidado.setLaboradasOriginal(novedadesLaboradas);

        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);

        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));
    }

    
    @Test
    public void pruebaGenerica2() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        // ausentismo
        DatosNovedades a = new DatosNovedades();
        /*a.setCotizacion(0L);
        a.setDias(6D);
        a.setFechaPago(formatter.parse("2017-08-08"));
        a.setIbc(666334D);
        a.setIngreso(false);
        a.setRetiro(false);
        a.setTasa(0.0);
        a.setTipoPlanilla(TipoPlanilla.E);
        a.setNumeroFormulario(numeroFormulario);
        novedadesAusentismo.add(a);*/

        // laboradas
        DatosNovedades l = new DatosNovedades();
        l.setCotizacion(36600L);
        l.setDias(30D);
        l.setFechaPago(formatter.parse("2017-08-08"));
        l.setIbc(1500000D);
        l.setIngreso(false);
        l.setRetiro(false);
        l.setTasa(2.436);
        l.setTipoPlanilla(TipoPlanilla.$);
        l.setNumeroFormulario(numeroFormulario);

        novedadesLaboradas.add(l);

        /*DatosNovedades l2 = new DatosNovedades();
        l2.setCotizacion(5600L);
        l2.setDias(24D);
        l2.setFechaPago(formatter.parse("2017-10-01"));
        l2.setIbc(1061984D);
        l2.setIngreso(true);
        l2.setRetiro(false);
        l2.setTasa(0.522);
        l2.setTipoPlanilla(TipoPlanilla.E);
        l2.setNumeroFormulario(numeroFormulario);*/
        // novedadesLaboradas.add(l2);

        // esperada
        EstadoCuenta ec = estadoCuentaActual.cotizacion(34000L).dias(30).ibc(828116L).tasa(4.35).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        consolidado.setAusentismoOriginal(novedadesAusentismo);
        consolidado.setLaboradasOriginal(novedadesLaboradas);

        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);

        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));
    }
    
    @Test
    public void pruebaGenerica3() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        // esperada
        EstadoCuenta ec = estadoCuentaActual.cotizacion(6700L).dias(30).ibc(737717L).tasa(0.522).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        consolidado.setAusentismoOriginal(novedadesAusentismo);
        consolidado.setLaboradasOriginal(novedadesLaboradas);
        
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);

        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));
    }
    
    @Test
    public void pruebaGenerica5() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {


        // laboradas
        DatosNovedades l1 = new DatosNovedades();
        l1.setCotizacion(6200L);
        l1.setDias(27D);
        l1.setFechaPago(formatter.parse("2017-08-08"));
        l1.setIbc(1181000D);
        l1.setIngreso(false);
        l1.setRetiro(false);
        l1.setTasa(0.552);
        l1.setTipoPlanilla(TipoPlanilla.N);
        l1.setNumeroFormulario(numeroFormulario);
        l1.setLey("2388");
        novedadesLaboradas.add(l1);
        
        
        DatosNovedades l2 = new DatosNovedades();
        l2.setCotizacion(6000L);
        l2.setDias(27D);
        l2.setFechaPago(formatter.parse("2017-08-08"));
        l2.setIbc(1148000D);
        l2.setIngreso(false);
        l2.setRetiro(false);
        l2.setTasa(0.552);
        l2.setTipoPlanilla(TipoPlanilla.E);
        l2.setNumeroFormulario(numeroFormulario);
        l2.setLey("1747");
        novedadesLaboradas.add(l2);
        
        
        DatosNovedades l3 = new DatosNovedades();
        l3.setCotizacion(-6000L);
        l3.setDias(-24D);
        l3.setFechaPago(formatter.parse("2017-08-08"));
        l3.setIbc(-1020000D);
        l3.setIngreso(false);
        l3.setRetiro(false);
        l3.setTasa(-0.552);
        l3.setTipoPlanilla(TipoPlanilla.N);
        l3.setNumeroFormulario(numeroFormulario);
        l3.setLey("2388");
        novedadesAusentismo.add(l3);
        
        DatosNovedades l4 = new DatosNovedades();
        l4.setCotizacion(6000L);
        l4.setDias(27D);
        l4.setFechaPago(formatter.parse("2017-08-08"));
        l4.setIbc(1148000D);
        l4.setIngreso(false);
        l4.setRetiro(false);
        l4.setTasa(0.552);
        l4.setTipoPlanilla(TipoPlanilla.E);
        l4.setNumeroFormulario(numeroFormulario);
        l4.setLey("1747");
        novedadesAusentismo.add(l4);

   
        // esperada
        EstadoCuenta ec = estadoCuentaActual.cotizacion(7300L).dias(30).ibc(1400000L).tasa(0.522).build();
        // 27 dias, 3 ausentismo, ibc:1181000, ibcaus:128000, cot: 6200

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        consolidado.setAusentismoOriginal(novedadesAusentismo);
        consolidado.setLaboradasOriginal(novedadesLaboradas);
        
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);

        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));
        vc.getPagos().forEach(System.out::println);
    }
    

    // @Test
    public void probar2388IngresoSinAusentismo() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        DatosNovedades n = new DatosNovedades();
        n.setCotizacion(14800L);
        n.setDias(30D);
        n.setFechaPago(formatter.parse("2017-08-17"));
        n.setIbc(2821000D);
        n.setIngreso(true);
        n.setRetiro(false);
        n.setTasa(0.522);
        n.setTipoPlanilla(TipoPlanilla.E);
        novedadesLaboradas.add(n);

        EstadoCuenta ec = estadoCuentaActual.cotizacion(14800L).dias(27).ibc(2821000L).tasa(0.522).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);

        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));

        assertEquals(vc.getIbc(), new Double(2821000));
        assertEquals(vc.getDias(), new Double(27));
        assertEquals(vc.getCotizacion(), new Double(14800));
    }

    // @Test
    public void probar2388IngresoConAusentismoYlaborados() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        DatosNovedades n = new DatosNovedades();
        n.setCotizacion(14800L);
        n.setDias(15D);
        n.setFechaPago(formatter.parse("2017-08-17"));
        n.setIbc(2821000D);
        n.setIngreso(false);
        n.setRetiro(false);
        n.setTasa(0.522);
        n.setTipoPlanilla(TipoPlanilla.E);
        novedadesLaboradas.add(n);

        DatosNovedades n2 = new DatosNovedades();
        n.setCotizacion(14800L);
        n.setDias(2D);
        n.setFechaPago(formatter.parse("2017-08-17"));
        n.setIbc(2821000D);
        n.setIngreso(false);
        n.setRetiro(true);
        n.setTasa(0.522);
        n.setTipoPlanilla(TipoPlanilla.E);
        novedadesAusentismo.add(n);

        EstadoCuenta ec = estadoCuentaActual.cotizacion(14800L).dias(30).ibc(2821000L).tasa(0.522).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);

        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));

        assertEquals(vc.getIbc(), new Double(2821000));
        assertEquals(vc.getDias(), new Double(27));
        assertEquals(vc.getCotizacion(), new Double(14800));
    }

    // @Test
    public void probar2388IngresoSoloLaborado() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        DatosNovedades n = new DatosNovedades();
        n.setCotizacion(2000L);
        n.setDias(11D);
        n.setFechaPago(formatter.parse("2017-08-17"));
        n.setIbc(377703D);
        n.setIngreso(true);
        n.setRetiro(false);
        n.setTasa(0.522);
        n.setTipoPlanilla(TipoPlanilla.E);
        novedadesLaboradas.add(n);

        EstadoCuenta ec = estadoCuentaActual.cotizacion(2700L).dias(15).ibc(515050L).tasa(0.522).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));

        assertEquals(vc.getIbc(), new Double(2821000));
        assertEquals(vc.getDias(), new Double(27));
        assertEquals(vc.getCotizacion(), new Double(14800));
    }

    // @Test
    public void probar2388tipoCotizanteMaximoYminimoEn1() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        afiliado.getCondicion().setIbcMaximo(1);
        afiliado.getCondicion().setIbcMinimo(1);

        DatosNovedades n = new DatosNovedades();
        n.setCotizacion(2200L);
        n.setDias(2D);
        n.setFechaPago(formatter.parse("2017-08-17"));
        n.setIbc(49182D);
        n.setIngreso(true);
        n.setRetiro(false);
        n.setTasa(4.35);
        n.setTipoPlanilla(TipoPlanilla.E);
        novedadesLaboradas.add(n);

        EstadoCuenta ec = estadoCuentaActual.cotizacion(2200L).dias(2).ibc(49182L).tasa(4.35).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));

        // assertEquals(vc.getIbc(), new Double(2821000));
        assertEquals(vc.getIbc(), new Double(49182));
        // assertEquals(vc.getCotizacion(), new Double(14800));
    }

    // @Test
    public void probar2388xx() throws ParseException, ReprocesoAfiliadoCanceladoExcepcion {

        DatosNovedades n = new DatosNovedades();
        n.setCotizacion(393600L);
        n.setDias(30D);
        n.setFechaPago(formatter.parse("2017-08-17"));
        n.setIbc(9048000D);
        n.setIngreso(true);
        n.setRetiro(false);
        n.setTasa(4.35);
        n.setTipoPlanilla(TipoPlanilla.E);
        novedadesLaboradas.add(n);

        EstadoCuenta ec = estadoCuentaActual.cotizacion(14800L).dias(30).ibc(2821000L).tasa(4.35).build();

        consolidado.setAusentismo(novedadesAusentismo);
        consolidado.setLaboradas(novedadesLaboradas);
        when(reprocesoEsperadasDao.consultarNovedadesAfiliado(any(Afiliado.class), Mockito.anyString()))
                .thenReturn(consolidado);
        when(actualizacionCT.procesarCambioCT(any(EstadoCuenta.class), any(Afiliado.class), Mockito.anyString(),
                any(List.class))).thenReturn(ec);
        ResultadoRecalculo vc = sut.calcularValores(ec, afiliado, periodo, Optional.of(numeroFormulario),
                Optional.of(resumenNovedad));

        // assertEquals(vc.getIbc(), new Double(2821000));
        assertEquals(vc.getDias(), new Double(30));
        // assertEquals(vc.getCotizacion(), new Double(14800));
    }

}
