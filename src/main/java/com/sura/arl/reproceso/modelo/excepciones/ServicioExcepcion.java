package com.sura.arl.reproceso.modelo.excepciones;

/**
 * Excepcion unchecked, la utiliza las clases de servicio para propagar alg�n
 * error de ejecuci�n.
 * 
 * @author andegaso
 *
 */
public class ServicioExcepcion extends RuntimeException {

    private static final long serialVersionUID = 3121381383393533627L;

    public ServicioExcepcion(String message) {
        super(message);
    }

}
