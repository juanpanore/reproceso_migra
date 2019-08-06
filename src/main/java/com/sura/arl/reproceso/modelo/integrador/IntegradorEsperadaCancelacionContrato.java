package com.sura.arl.reproceso.modelo.integrador;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaCancelacionContrato extends IntegradorEsperada {

    protected String poliza;

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaCancelacionContrato{" + "poliza=" + poliza + '}';
    }

}
