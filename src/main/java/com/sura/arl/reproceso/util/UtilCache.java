package com.sura.arl.reproceso.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;

public class UtilCache {

    static Map<String, Object> cache = new ConcurrentHashMap<String, Object>();

    private UtilCache() {

    }

    public static void agregar(String clave, Object valor) {
        if (!cache.containsKey(clave)) {
            cache.put(clave, valor);
        }
    }

    public static Object obtener(String clave) {
        return cache.get(clave);
    }

    public static void borrar(String clave) {
        cache.remove(clave);
    }
    
    @Scheduled(cron = "0 0 0/6 * * ?", zone = "GMT-5:00")
    public void resetear() {
        cache.clear();
    }
}
