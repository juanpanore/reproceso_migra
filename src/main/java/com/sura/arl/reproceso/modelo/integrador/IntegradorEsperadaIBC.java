package com.sura.arl.reproceso.modelo.integrador;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaIBC extends IntegradorEsperada {

    private String ibc;

    public String getIbc() {
        return ibc;
    }

    public void setIbc(String ibc) {
        this.ibc = ibc;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaIBC{" + "ibc=" + ibc + ", tipo=" + tipo + '}';
    }

}
