package com.sura.arl.reproceso.util;

import java.util.Objects;

public class Cadenas {

    public static void requiereNoNuloNoVacio(String cadena, String mensaje) {

        if (Objects.isNull(cadena)) {
            throw new IllegalArgumentException(mensaje);
        } else if (cadena.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }

    }

}
