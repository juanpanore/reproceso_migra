package com.sura.arl.reproceso.actores.notificacion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationContext;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.ErrorProceso.EstadoError;
import com.sura.arl.estadocuenta.servicios.ErroresProcesoServicio;
import com.sura.arl.reproceso.accesodatos.NotificacionLimitePagoDao;
import com.sura.arl.reproceso.modelo.TipoGeneracion;
import com.sura.arl.reproceso.modelo.notificacion.EstadoNotificacionLimitePago;
import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.util.JSON;
import com.sura.arl.reproceso.util.Objetos;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.Done;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

public class CalcularFechaLimitePago extends AbstractActor {

    private final NotificacionLimitePagoDao dao;
    private final VariablesEntorno entorno;
    private final ErroresProcesoServicio errorProcesoServicio;
    private Materializer materializer;
    private final LoggingAdapter LOG = Logging.getLogger(context().system(), this);
    private final String DESC_FECHA_LIMITE_PAGO_NO_CALCULADA = "FECHA LIMITE PAGO NO CALCULADA";

    public CalcularFechaLimitePago(ApplicationContext contexto) {

        dao = contexto.getBean(NotificacionLimitePagoDao.class);
        entorno = contexto.getBean(VariablesEntorno.class);
        errorProcesoServicio = contexto.getBean(ErroresProcesoServicio.class);
        materializer = ActorMaterializer.create(getContext());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, this::calcularFechaLimitePago).build();
    }

    private void calcularFechaLimitePago(String mensaje) {
        
        SolicitudConsultaPolizasNotificacion solicitud = JSON.jsonToObjeto(mensaje,
                SolicitudConsultaPolizasNotificacion.class);
        
        LOG.debug("Calculando fecha limite pago para {} contratos", solicitud.coberturas.size());
        
        final ActorRef sender = getSender();

        Source.from(solicitud.coberturas).map(cobertura -> {
            
            NotificacionLimitePago registro = new NotificacionLimitePago();
            registro.setPoliza(cobertura.getPoliza());
            registro.setEstadoNotificacion(EstadoNotificacionLimitePago.NUEVO);
            registro.setPeriodo(solicitud.periodo);

            Optional<Date> fechaLimitePago = dao.consultaFechaLimitePago(cobertura.getPoliza(), cobertura.getPeriodo());

            if (!fechaLimitePago.isPresent()) {
                errorProcesoServicio.registrarErrorProcesoUnico(obtenerErrorProceso(cobertura.getPoliza(),
                        cobertura.getPeriodo(), CatalogoErrores.FECHA_LIMITE_PAGO_NO_CALCULADA,
                        DESC_FECHA_LIMITE_PAGO_NO_CALCULADA, solicitud.tipoGeneracion));
            } else {
                registro.setFechaLimitePago(fechaLimitePago.get());
            }

            return registro;
        }).filter(registro -> !Objetos.esNulo(registro.getFechaLimitePago())).grouped(100)
                .runForeach(registros -> dao.ingresar(registros), materializer).whenComplete((done, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        LOG.error("Error calculando la fecha limite pago {}", e);
                    } else {
                        sender.tell(Done.getInstance(), ActorRef.noSender());
                    }
                });
    }

    public static class SolicitudConsultaPolizasNotificacion {

        public final String periodo;
        public final List<Cobertura> coberturas;
        public final TipoGeneracion tipoGeneracion;

        public SolicitudConsultaPolizasNotificacion(String periodo, List<Cobertura> coberturas,
                TipoGeneracion tipoGeneracion) {
            this.periodo = periodo;
            this.coberturas = coberturas;
            this.tipoGeneracion = tipoGeneracion;
        }
    }

    private ErrorProceso obtenerErrorProceso(String poliza, String periodo, String codError, String descripcion,
            TipoGeneracion tipoGeneracion) {

        ErrorProceso errorProceso = ErrorProceso.builder().codError(codError)
                .codigoProceso(entorno.getValor(VariablesEntorno.ID_PROCESO_NOTIFICACION))
                .usuarioRegistro(entorno.getValor(VariablesEntorno.DNI_INGRESA))
                .tipoGeneracion(tipoGeneracion.getEquivalencia())
                .periodoGeneracion(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")))
                .fechaRegistro(Calendar.getInstance().getTime()).npoliza(poliza).periodo(periodo)
                .observacion(descripcion).estadoError(EstadoError.POR_CORREGIR).build();

        return errorProceso;
    }

}
