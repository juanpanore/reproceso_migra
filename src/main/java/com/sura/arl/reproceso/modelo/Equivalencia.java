package com.sura.arl.reproceso.modelo;

/**
 *
 * @author pragma.co
 */
public interface Equivalencia {

    public String getEquivalencia();

    public static <E extends Enum<E> & Equivalencia> E getEquivalencia(String code, Class<E> enumm) {

        for (E e : enumm.getEnumConstants()) {
            if (e.getEquivalencia().compareToIgnoreCase(code) == 0) {
                return e;
            }
        }
        return null;
    }
}
