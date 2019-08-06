package com.sura.arl.reproceso.modelo.integrador;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaCambioDocumento extends IntegradorEsperada {

    protected String dniAfiliado;

    public String getDniAfiliado() {
        return dniAfiliado;
    }

    public void setDniAfiliado(String dniAfiliado) {
        this.dniAfiliado = dniAfiliado;
    }

    @Override
    public String toString() {
        return "IntegradorEsperadaCambioDocumento{" + "dniAfiliado=" + dniAfiliado + '}';
    }

}
