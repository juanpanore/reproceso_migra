package com.sura.arl.reproceso.modelo.notificacion;

@FunctionalInterface
public interface TriFunction<T,U,S,R> {
    
    R apply(T t, U u, S s);
}
