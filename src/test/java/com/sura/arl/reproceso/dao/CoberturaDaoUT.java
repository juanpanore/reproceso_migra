package com.sura.arl.reproceso.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.general.accesodatos.QueriesDAO;
import com.sura.arl.reproceso.accesodatos.CoberturaDao;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class CoberturaDaoUT {

    CoberturaDao sut;

    @Mock
    VariablesEntorno entorno;

    @Mock
    JdbcCustomTemplate jdbcTemplate;

    @Mock
    QueriesDAO queriesDAO;

    @Before
    public void inicializacion() {
        sut = new CoberturaDao();
        sut.setJdbcTemplate(jdbcTemplate);
        sut.setVarEntorno(entorno);
        sut.setQueriesDAO(queriesDAO);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConsultarCoberturasNotificacionEmpresa() {

        when(jdbcTemplate.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(obtenerCoberturas());
        List<Cobertura> resultado = sut.consultarCoberturasNotificacionEmpresa(Mockito.anyString());
        assertEquals(1, resultado.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConsultarCoberturasNotificacionVoluntarios() {

        when(jdbcTemplate.query(anyString(), anyMapOf(String.class, Object.class), any(RowMapper.class)))
                .thenReturn(obtenerCoberturas());
        List<Cobertura> resultado = sut.consultarCoberturasNotificacionVoluntarios(Mockito.anyString());
        assertEquals(1, resultado.size());
    }

    private List<Cobertura> obtenerCoberturas() {
        return Arrays.asList(new Cobertura("0000123", "201712"));
    }
}
