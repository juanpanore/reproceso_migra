package com.sura.arl.reproceso.servicios.notificacion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.util.UtilFechas;

@Service
public class NotificacionLimitePagoIntegracionesServicio {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionLimitePagoIntegracionesServicio.class);

    public void guardarArchivoNotificacionFtp(String nombreArchivo, String directorio, InputStream archivo,
            String servidor, int puerto, String usuario, String contrasenia) throws SocketException, IOException {

        FTPClient ftp = new FTPClient();
        ftp.connect(servidor, puerto);
        int respuesta = ftp.getReplyCode();
        LOG.info("Respuesta ftp {} ", respuesta);
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            ftp.disconnect();
            throw new IOException("Excepcion al conectarse con el servidor FTP");
        }

        ftp.login(usuario, contrasenia);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
        boolean cambiodirectorio = ftp.changeWorkingDirectory(directorio);
        LOG.info("respuesta directorio {} ", cambiodirectorio);
        ftp.storeFile(nombreArchivo, archivo);
        ftp.disconnect();
    }

    public Optional<ByteArrayOutputStream> generarCsvNotificacion(List<NotificacionLimitePago> registros) {

        if (registros.size() == 0) {
            return Optional.empty();
        }

        final String[] FILE_HEADER_DEVOLUCIONES = { "Fecha", "Nit", "Empresa", "Correo Electronico Empleador",
                "Periodo", "Fecha Limite de Pago", "Key" };

        final CellProcessor[] processors = new CellProcessor[] { new org.supercsv.cellprocessor.Optional(),
                new org.supercsv.cellprocessor.Optional(), new org.supercsv.cellprocessor.Optional(),
                new org.supercsv.cellprocessor.Optional(), new org.supercsv.cellprocessor.Optional(),
                new org.supercsv.cellprocessor.Optional(), new org.supercsv.cellprocessor.Optional() };

        SimpleDateFormat sdf1 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM", new Locale("es", "CO"));

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.ISO_8859_1);
                ICsvMapWriter csvWriter = new CsvMapWriter(writer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);) {

            csvWriter.writeHeader(FILE_HEADER_DEVOLUCIONES);

            for (NotificacionLimitePago registro : registros) {
                registro.setStrFechaNotificacion(obtenerFormatoFechaMesMayusculas(sdf1.format(new Date())));
                registro.setStrFechaLimitePago(
                        obtenerFormatoFechaMesMayusculas(sdf1.format(registro.getFechaLimitePago())));
                registro.setStrPeriodo(
                        sdf2.format(UtilFechas.parsear(registro.getPeriodo().concat("01"), UtilFechas.FORMATO_DEFECTO))
                                .toUpperCase());
                if (registro.getCorreoAfiliado().isPresent()) {
                    csvWriter.write(obtenerMapaValoresNotificacionCsv(registro, FILE_HEADER_DEVOLUCIONES),
                            FILE_HEADER_DEVOLUCIONES, processors);
                }
            }
            csvWriter.flush();
            LOG.info("Finalizacion generacion de archivos csv fecha limite pago");
            return Optional.ofNullable(bos);
        } catch (IOException e) {
            LOG.error("Se ha generado error al generar csv fecha limite pago {} ", e);
        }
        return Optional.empty();
    }

    private Map<String, Object> obtenerMapaValoresNotificacionCsv(NotificacionLimitePago notificacion,
            String[] header) {

        Map<String, Object> registro = new HashMap<>();
        registro.put(header[0], notificacion.getStrFechaNotificacion());
        registro.put(header[1], notificacion.getDni());
        registro.put(header[2], notificacion.getNombreAfiliado());
        registro.put(header[3], notificacion.getCorreoAfiliado().get());
        registro.put(header[4], notificacion.getStrPeriodo());
        registro.put(header[5], notificacion.getStrFechaLimitePago());
        registro.put(header[6], notificacion.getDni().concat(notificacion.getCorreoAfiliado().get()));
        return registro;
    }
    
    private String obtenerFormatoFechaMesMayusculas(String fecha) {

        return fecha.substring(0, 6).concat(String.valueOf(fecha.charAt(6)).toUpperCase()).concat(fecha.substring(7));
    }
}
