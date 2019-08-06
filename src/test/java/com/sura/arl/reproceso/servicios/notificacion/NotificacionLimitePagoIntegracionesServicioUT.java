package com.sura.arl.reproceso.servicios.notificacion;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.reproceso.modelo.notificacion.EstadoNotificacionLimitePago;
import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.servicios.notificacion.NotificacionLimitePagoIntegracionesServicio;
import com.sura.arl.reproceso.util.UtilFechas;

@RunWith(MockitoJUnitRunner.class)
public class NotificacionLimitePagoIntegracionesServicioUT {

    @Mock
    NotificacionLimitePagoIntegracionesServicio sut;
    
    
    @Before
    public void inicializacion() throws Exception {
        sut = new NotificacionLimitePagoIntegracionesServicio();
    }
    
    @Test
    public void generarCsvNotificacion() {
       
        Optional<ByteArrayOutputStream> resultado = sut.generarCsvNotificacion(Arrays.asList(obtenerNotificacionesLimitePago()));
        assertTrue(resultado.isPresent());
    }
    
    private NotificacionLimitePago obtenerNotificacionesLimitePago(){
        
        NotificacionLimitePago notificacion = new NotificacionLimitePago();
        notificacion.setConsecutivo(1l);
        notificacion.setCorreoAfiliado(Optional.of("prueba@prueba.com"));
        notificacion.setDni("C1");
        notificacion.setEstadoNotificacion(EstadoNotificacionLimitePago.NUEVO);
        notificacion.setFechaLimitePago(UtilFechas.fechaActualSinHora());
        notificacion.setFechaNotificacion(UtilFechas.fechaActualSinHora());
        notificacion.setNombreAfiliado("Afiliado 1");
        notificacion.setNombreArchivo("CP_Archivo_1");
        notificacion.setNroAfiliados(1l);
        notificacion.setPeriodo("201712");
        notificacion.setPoliza("9000123433");
        notificacion.setStrFechaLimitePago("2017-12-05");
        notificacion.setStrFechaNotificacion("2017-12-02");
        notificacion.setUsuarioOperacion("prueba");
        
        return notificacion;
        
    }
}
