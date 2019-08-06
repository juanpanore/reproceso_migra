package com.sura.arl.reproceso.modelo.integrador;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pragma.co
 */
public class IntegradorEsperada {
      
    public enum TipoMensaje {
        AFILIACION, //REGISTRO,
        RETIRO,
        MOVER_COBERTURA, //COBERTURA,
        ANULACION, //BORRADO,
        CAMBIO_TASA_CT, //CAMBIOTASA,
        CAMBIO_CENTRO_TRABAJO, //CAMBIOCENTROTRABAJO,
        CANCELACION_CONTRATO, //CANCELACIONCONTRATO,
        CAMBIO_DOCUMENTO, //CAMBIODOCUMENTO,
        AFILIACION_IND, //NUEVOCONTRATO, 
        CAMBIO_ACTIVIDAD_IND, //CAMBIOACTIVIDADINDEPENDIENTE,
        REVERSA_LEGALIZACION, //REVERSALEGALIZACION,
        ACTUALIZACIONNOVEDAD, 
        CAMBIO_CTP,
        INDEPENDIENTES_VOLV,
        REPROCESO_FLUJO_COMPLETO;
    };
    
    protected Long id;

    protected TipoMensaje tipo;

    protected String dniUsuario;

    /**
     * Integrado Esperada Registro
     */
    private static final Map<TipoMensaje, Class<? extends IntegradorEsperada>> IER = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public TipoMensaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensaje tipo) {
        this.tipo = tipo;
    }

    public String getDniUsuario() {
        return dniUsuario;
    }

    public void setDniUsuario(String dniUsuario) {
        this.dniUsuario = dniUsuario;
    }

    public static void addIntegradorEsperadaRegistro(TipoMensaje tipo, Class<? extends IntegradorEsperada> clazz) {
        IER.put(tipo, clazz);
    }

    public static Class<? extends IntegradorEsperada> getIntegradorEsperadaRegistro(TipoMensaje tipo) throws ClassNotFoundException {

        if (!IER.containsKey(tipo)) {
            throw new ClassNotFoundException("Clase no registrada de tipo:" + tipo);
        }
        return IER.get(tipo);
    }

    @Override
    public String toString() {
        return "IntegradorEsperada{" + "tipo=" + tipo + ", dniUsuario=" + dniUsuario + '}';
    }

}
