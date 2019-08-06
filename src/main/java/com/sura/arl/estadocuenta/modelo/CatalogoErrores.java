package com.sura.arl.estadocuenta.modelo;

public class CatalogoErrores {
    
    public static final String TASA_NO_ENCONTRADA = "PE004";
    public static final String IBC_NO_ENCONTRADO = "PE005";
    public static final String DIAS_NO_ENCONTRADO = "PE006";
    public static final String ERROR_NO_CONTROLADO = "PE007";
    public static final String MULTIPLES_COBERTURAS = "PE008";
    public static final String COBERTURA_NO_ENCONTRADA = "PE009";
    public static final String COBERTURA_INVALIDA = "PE010";
    public static final String COBERTURA_REPORTADA_DISTINTA_A_AFILIACION = "PE011";
    public static final String ES_UN_RENE = "PE012"; //temporal
    public static final String ES_UN_ENRQ = "PE013";
    public static final String AMU = "PE014";
    public static final String DIF_CALCULO_DIAS = "PE015";
    public static final String DIF_CALCULO_COT = "PE016";
    public static final String ERROR_CAMBIO_CT = "PE017";
    
    public static final String FECHA_LIMITE_PAGO_NO_CALCULADA = "PN001";
    public static final String CORREO_NOTIFICACION_VACIO = "PN002";
    public static final String NRO_AFILIADOS_CERO = "PN003";
    public static final String ARCHIVO_NOTIFICACION_NO_GENERADO = "PN004";
    public static final String CORREO_NOTIFICACION_INVALIDO = "PN005";

    private CatalogoErrores() {
        super();
    }
}