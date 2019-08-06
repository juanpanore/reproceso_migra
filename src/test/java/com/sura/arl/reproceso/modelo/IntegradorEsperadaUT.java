package com.sura.arl.reproceso.modelo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;

/**
 *
 * @author pragma.co
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegradorEsperadaUT {

    @Test
    public void addIntegradorEsperadaRegistroTest() {

        String test = "TEST";
        IntegradorEsperada iE = new IntegradorEsperada() {

            @Override
            public String toString() {
                return "TEST";
            }
        };

        iE.setTipo(IntegradorEsperada.TipoMensaje.AFILIACION);

        IntegradorEsperada.addIntegradorEsperadaRegistro(IntegradorEsperada.TipoMensaje.AFILIACION, iE.getClass());

        try {
            Assert.assertEquals(iE.getClass().getName(), IntegradorEsperada.getIntegradorEsperadaRegistro(IntegradorEsperada.TipoMensaje.AFILIACION).getName());
        } catch (ClassNotFoundException ex) {
            Assert.fail();
        }

        try {
            Assert.assertEquals(iE.getClass().getName(), IntegradorEsperada.getIntegradorEsperadaRegistro(IntegradorEsperada.TipoMensaje.RETIRO).getName());
            Assert.fail();
        } catch (ClassNotFoundException ex) {

        }

    }

}
