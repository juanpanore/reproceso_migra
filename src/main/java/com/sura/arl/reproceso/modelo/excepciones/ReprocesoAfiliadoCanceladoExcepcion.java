package com.sura.arl.reproceso.modelo.excepciones;

public class ReprocesoAfiliadoCanceladoExcepcion extends Exception {

    private static final long serialVersionUID = -3973623227645827425L;
    
    public ReprocesoAfiliadoCanceladoExcepcion(){
        super();
    }
    
    public ReprocesoAfiliadoCanceladoExcepcion(String mensaje){
        super(mensaje);
    }
}
