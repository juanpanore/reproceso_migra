package com.sura.arl.reproceso.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperada;

import java.lang.reflect.Type;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperadaAdapter implements JsonDeserializer<IntegradorEsperada>, JsonSerializer<IntegradorEsperada>{
    
    private static final String PROP_NAME = "tipo";
    
    @Override
    public IntegradorEsperada deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            String tipo = "";
        try{
            tipo = je.getAsJsonObject().getAsJsonPrimitive(PROP_NAME).getAsString();
        }catch(Exception e) {
            throw  new RuntimeException(String.format("Atributo \"%s\" no encontrado en JSON.",PROP_NAME), e);
        }   
        try{
            Class<? extends IntegradorEsperada> cls = IntegradorEsperada.getIntegradorEsperadaRegistro(IntegradorEsperada.TipoMensaje.valueOf(tipo));
            return jdc.deserialize(je, cls);
        }catch(ClassNotFoundException cnfe){
            throw  new RuntimeException(String.format("Clase de tipo %s no encontrada.",tipo), cnfe);
        }
    }

    @Override
    public JsonElement serialize(IntegradorEsperada t, Type type, JsonSerializationContext jsc) {
        
        return jsc.serialize(t).getAsJsonObject();
    } 
    
}
