package com.sura.arl.reproceso.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UtilObjetos {

    private static final Logger LOG = LoggerFactory.getLogger(UtilObjetos.class);

    private UtilObjetos() {

    }

    @SuppressWarnings("unchecked")
    public static Object clonar(Object obj) {
        try {
            Object clone = obj.getClass().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {

                field.setAccessible(true);
                if (field.get(obj) == null || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (field.getType().isPrimitive() || field.getType().equals(String.class)
                        || (field.getType().getSuperclass() != null
                                && field.getType().getSuperclass().equals(Number.class))
                        || field.getType().equals(Boolean.class)
                        || (field.getType().getSuperclass() != null
                                && field.getType().getSuperclass().equals(Enum.class))
                        || field.getType().equals(Date.class)) {
                    field.set(clone, field.get(obj));
                } else if (field.getType().equals(List.class)) {

                    List<Object> list = (List<Object>) field.get(obj);
                    List<Object> listCloned = list.stream().map(UtilObjetos::clonar).collect(Collectors.toList());

                    field.set(clone, listCloned);

                } else {
                    Object childObj = field.get(obj);
                    field.set(clone, childObj == obj ? clone : clonar(field.get(obj)));
                }
            }
            return clone;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static Double nvl(Double value, Double alternateValue) {
        if (value == null)
            return alternateValue;

        return value;
    }

    /**
     * @param num1
     * @param num2
     * @return
     */
    public static Double porcentajeDiferenciaEntreNumeros(Double num1, Double num2) {

        Double res;
        if (num1 < num2) {
            res = Math.abs(1-(num1 / num2));
        } else if (num2 < num1) {
            res = Math.abs(1-(num2 / num1));
        } else {
            res = 100D;
        }
        return res;
    }
    
}
