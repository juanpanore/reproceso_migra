package com.sura.arl.reproceso.modelo.excepciones;

import org.springframework.dao.DataAccessException;

public class AccesoDatosExcepcion extends DataAccessException {

    private static final long serialVersionUID = -3973623227645827425L;

    public AccesoDatosExcepcion(String msg) {
        super(msg);
    }

    public AccesoDatosExcepcion(String msg, Throwable cause) {
        super(msg, cause);
    }

}
