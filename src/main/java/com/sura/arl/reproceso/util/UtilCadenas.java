package com.sura.arl.reproceso.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UtilCadenas {

    public static final String SEPARADOR_LINEA = "\r\n";
    public static final String SIN_NOMBRE = "SIN NOMBRE";

    private UtilCadenas() {
    }

    public static String padLeft(String s, int n) {
        String format = "%" + n + "s";
        return String.format(format, s == null ? "" : s);
    }

    public static String padRight(String s, int n) {
        String format = "%1$-" + n + "s";
        return String.format(format, s == null ? "" : s);
    }

    public static boolean contieneSoloNumeros(String s) {
        return s.matches("[0-9]+");
    }

    public static void requiereEsNumero(String val, String mensaje) {
        if (!contieneSoloNumeros(val)) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    public static boolean esNuloOVacio(String val) {
        if (val == null || val.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static String normalizarNombreAportante(final String nombreAportante) {
        String nombreConFormato = nombreAportante;
        if (nombreConFormato != null) {
            nombreConFormato = borrarPrefijoAsteriscos(nombreConFormato);
            if (nombreConFormato.contains("***")) {
                String[] a = nombreConFormato.split("\\*\\*\\*");
                return String.format("%s %s", a[1], a[0]).trim();
            } else if (("* **").equals(nombreConFormato) || "".equals(nombreConFormato)) {
                return SIN_NOMBRE;
            } else if (nombreConFormato.contains("**")) {
                String[] a = nombreConFormato.split("\\*\\*");
                String[] b = a[0].split("\\*");
                return String.format("%s %s %s", a[1], b[0], b[1]).trim();
            } else {
                return nombreConFormato.trim();
            }
        }

        return null;
    }
    
    /**
     * Borra el prefijo de la cadena si:
     * - si tiene combinación  de asteriscos y espacios en blanco.
     * - si solo son asteriscos
     * - si solo son espacios en blanco
     * Solo borra hasta que encuentre el primer símbolo diferente a los mencionados, los siguientes caracteres se conservan.
     * 
     * @param cadena
     * @return cadena sin prefijo
     */
    public static String borrarPrefijoAsteriscos(String cadena) {

        if (cadena == null) {
            return null;
        }
        Matcher m = Pattern.compile("[^*|^\\s]").matcher(cadena);

        return m.find() ? cadena.substring(m.start()) : "";
    }

    public static boolean sonIguales(String a, String b) {
        return a.equals(b);
    }

    public static void requiereCumplaPatron(String s, String patron, String mensaje) {
        if (!s.matches(patron)) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    public static void requiereCumpleLongitud(String s, int longitudMin, int longitudMax, String mensaje) {
        if (!(s.length() >= longitudMin && s.length() <= longitudMax)) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    public static void requiereNoNuloOVacio(String val, String mensaje) {
        if (esNuloOVacio(val)) {
            throw new IllegalArgumentException(mensaje);
        }
        ;
    }

    public static String cambiarAMayusculas(String texto) {
        if (texto != null) {

            return texto.toUpperCase();
        }

        return null;
    }

    public static String cortar(String cadena, int max) {
        if (cadena != null) {
            if (cadena.length() > max) {
                return cadena.substring(0, max);
            }
            return cadena;
        }

        return null;
    }
    
    public static String trim(String string) {

        if (string == null) {
            return "";
        } else {
            return string.trim();
        }
    }
    
}
