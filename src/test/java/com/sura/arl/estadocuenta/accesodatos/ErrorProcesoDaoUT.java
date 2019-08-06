package com.sura.arl.estadocuenta.accesodatos;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.ErrorProceso.EstadoError;
import com.sura.arl.general.accesodatos.AbstractDAO.JdbcCustomTemplate;
import com.sura.arl.reproceso.util.VariablesEntorno;

@RunWith(MockitoJUnitRunner.class)
public class ErrorProcesoDaoUT {

    @Mock
    JdbcCustomTemplate jdbcTemplate;

    @Mock
    VariablesEntorno entorno;

    @InjectMocks
    ErrorProcesoDao sut;

    @Test
    public void registrarError() {

        ErrorProceso errorProceso = ErrorProceso.builder().codError("01").estadoError(EstadoError.POR_CORREGIR)
                .usuarioRegistro("00000").build();

        sut.registrar(errorProceso);

        verify(jdbcTemplate).update(anyString(), anyMapOf(String.class, Object.class));

    }

    @Test
    public void actualizarEstadoErrorEspecifico() {

        String poliza = "1";
        String periodoGeneracion = "201804";
        String periodoCotizacion = "201803";
        EstadoError nuevoEstado = EstadoError.CORREGIDO;
        String codigoInconsistencia = "PE001";
        String dniAfiliado = "1";

        sut.actualizarEstado(poliza, periodoGeneracion, periodoCotizacion, codigoInconsistencia, nuevoEstado,
                dniAfiliado);

        verify(jdbcTemplate).update(anyString(), anyMapOf(String.class, Object.class));

    }

    @Test
    public void actualizarEstadoErroresAsociadoAEmpleado() {

        String poliza = "1";
        String periodoGeneracion = "201804";
        String periodoCotizacion = "201803";
        EstadoError nuevoEstado = EstadoError.CORREGIDO;
        String dniAfiliado = "1";

        sut.actualizarEstado(poliza, periodoGeneracion, periodoCotizacion, nuevoEstado, dniAfiliado);

        verify(jdbcTemplate).update(anyString(), anyMapOf(String.class, Object.class));

    }

}
