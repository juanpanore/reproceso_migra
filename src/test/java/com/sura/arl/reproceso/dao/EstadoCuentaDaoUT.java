package com.sura.arl.reproceso.dao;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.modelo.excepciones.CambiosEsperadosExcepcion;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class EstadoCuentaDaoUT {

    @InjectMocks
    EstadoCuentaDao sut;

    @Mock
    VariablesEntorno entorno;

    @Mock
    JdbcCustomTemplate jdbcTemplate;

    @SuppressWarnings("unchecked")
    @Test
    public void consultarCoberturasTest() {

        Date fechaProceso = new Date();

        Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Collections.emptyList());

        sut.consultarCoberturasXPeriodo(fechaProceso, 2);

        Mockito.verify(entorno, times(1)).getValor("consulta.estado.cuenta.coberturas");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consultarPagoDeAfiliadosXCoberturaTest() {

        Cobertura cobertura = new Cobertura();
        Date fechaProceso = new Date();

        Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Collections.emptyList());

        sut.marcarPagoDeAfiliados(cobertura, fechaProceso);

        Mockito.verify(entorno, times(1)).getValor("marcar.pago.estado.cuenta.afiliados");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consultarPagoDeAfiliadoXCoberturaTest() {

        Afiliado afiliado = new Afiliado();
        Cobertura cobertura = new Cobertura();
        afiliado.setCobertura(cobertura);
        Long numFormulario = 0L;

        Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Collections.emptyList());

        sut.marcarPagoDeAfiliado(numFormulario, afiliado);

        Mockito.verify(entorno, times(1)).getValor("marcar.pago.estado.cuenta.afiliado");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consultarAfiliadosXPeriodoTest() {

        String periodo = "012000";

        Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Collections.emptyList());

        sut.consultarAfiliadosXPeriodo(periodo);

        Mockito.verify(entorno, times(1)).getValor("consulta.estado.cuenta.afiliados.periodo");
    }

    @Test(expected = CambiosEsperadosExcepcion.class)
    public void actualizarSinCambiosTest() {
        Afiliado af = new Afiliado();
        af.setCobertura(new Cobertura());

        EstadoCuenta registro = EstadoCuenta.builder().afiliado(af).cotizacion(1L).dias(0).build();

        sut.actualizar(registro);
        verify(jdbcTemplate).update(anyString(), anyMapOf(String.class, Object.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void consultarXafiliadosXPeriodoTest() {

        String periodo = "012000";
        String dni = "0";

        Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class),
                Mockito.any(RowMapper.class))).thenReturn(Collections.emptyList());

        sut.consultarXafiliadoPeriodo(dni, periodo);

        Mockito.verify(entorno, times(1)).getValor("consulta.estado.cuenta.xAfiliadoPeriodo");
    }

}
