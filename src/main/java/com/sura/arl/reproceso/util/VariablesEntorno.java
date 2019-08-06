package com.sura.arl.reproceso.util;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class VariablesEntorno {

	public static final String RUTA_BASE = "ruta.base";
	public static final String DNI_INGRESA = "usuario.dniingresa";
	public static final String BD_HOST = "db.host";
	public static final String BD_PUERTO = "db.port";
	public static final String FUENTE_LEGALIZACION = "fuente.legalizacion";
	public static final String LIMITE_PILAS = "limite.pilas";
	public static final String POOL_SIZE = "pool.size";
	public static final String NIVEL_LOG = "nivel.log";
	public static final String NOMBRE_PROPERTYSOURCE_LINEACOMANDO = "nombrePS.lineacomando";
	public static final String NOMBRE_PROPERTYSOURCE_PARAMETROS = "nombrePS.parametros";
	public static final String LLAVE_LOGGER_RECAUDOS = "com.sura.arl.recaudos";
	public static final String ID_PROCESO_REPROCESO = "id.proceso.reproceso";
	public static final String ID_PROCESO_NOTIFICACION = "id.proceso.notificacion";

	@Autowired
	private Environment env;

	public String getValor(String var) {
		String valor = env.getProperty(var);
		if (valor == null) {
			throw new RuntimeException(MessageFormat.format("Variable de entorno ({0}) no encontrada", var));
		}
		return valor;
	}

	public void setValor(String key, String value) {
		System.setProperty(key, value);
	}
}
