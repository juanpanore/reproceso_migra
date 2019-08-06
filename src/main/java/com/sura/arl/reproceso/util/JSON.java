package com.sura.arl.reproceso.util;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;

public class JSON {

    private static Gson gson = new GsonBuilder()
            .setDateFormat("dd/MM/yyyy")
            .registerTypeAdapter(IntegradorEsperada.class, new IntegradorEsperadaAdapter())
            .create();

    private JSON() {

    }

    @SuppressWarnings("unchecked")
    public static <T> T jsonToObjeto(String json, Class<?> classObject) {
        try {
            return (T) gson.fromJson(json, classObject);
        } catch (Exception e) {
            throw new IllegalArgumentException("json msg not valid", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T jsonToObjeto(String json, Class<?> classObject, String formatoFecha) {
        try {
            Gson gson = new GsonBuilder().setDateFormat(formatoFecha).create();
            return (T) gson.fromJson(json, classObject);
        } catch (Exception e) {
            throw new RuntimeException("No pudo ser procesado el mensaje [" + json + "]");
        }
    }

    public static String objetoToJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Convierte un Map a String con formato JSON
     *
     * @param campos Map con nombre del campo, contenido del campo
     * @return String texto convertido
     */
    public static StringBuilder convertirAjson(Map<String, String> campos) {
        StringBuilder textoJson = new StringBuilder();
        return textoJson.append(JSON.objetoToJson(campos));
    }

}
