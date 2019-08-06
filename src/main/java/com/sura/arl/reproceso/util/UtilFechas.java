package com.sura.arl.reproceso.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Clase que contiene funciones utiles para operaciones con fechas.
 *
 */
public final class UtilFechas {

    public static final String FORMATO_DEFECTO = "yyyyMMdd";

    private UtilFechas() {

    }

    public static Date obtenerFecha(String dia, String mes, String agno) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dia));
        c.set(Calendar.MONTH, Integer.parseInt(mes) - 1);
        c.set(Calendar.YEAR, Integer.parseInt(agno));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static Date obtenerFechaSinHora(String dia, String mes, String agno) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dia));
        c.set(Calendar.MONTH, Integer.parseInt(mes) - 1);
        c.set(Calendar.YEAR, Integer.parseInt(agno));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static Date fechaActualSinHora() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date obtenerFechaSinHora(Date fecha) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTime(fecha);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static String[] convertirFechaAstring(Date fecha) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String fechaString = df.format(fecha);
        return fechaString.split("-");
    }

    public static Date parsearFecha(String fecha, String formato) {
        try {
            return new Date(new SimpleDateFormat(formato, Locale.ENGLISH).parse(fecha).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date fechaActualConHora() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    public static String fechaFormateadaActualConHora() {
        return formatear("dd-MM-yyyy hh:mm:ss", fechaActualConHora());
    }
    
    public static LocalDate fechaLocal(Date fecha){
        
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(fecha);
        
        return  LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));
    }

    public static String formatear(String formato, Date fecha) {
        if (fecha == null) {
            return null;
        }

        SimpleDateFormat formateador = new SimpleDateFormat(formato);
        return formateador.format(fecha);
    }
    
    public static Date parsear(String fecha, String formato) {
        try {
            SimpleDateFormat formateador = new SimpleDateFormat(formato);
            return formateador.parse(fecha);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date agregarDias(Date fecha, int dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.add(Calendar.DAY_OF_MONTH, dias);
        return calendar.getTime();
    }

    public static boolean esFechaAntes(String fecha, String otraFecha) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyymm");
        try {
            if (!formatter.parse(fecha).before(formatter.parse(otraFecha))) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    
}
