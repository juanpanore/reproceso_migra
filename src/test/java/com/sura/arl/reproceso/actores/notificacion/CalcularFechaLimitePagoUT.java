package com.sura.arl.reproceso.actores.notificacion;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.servicios.ErroresProcesoServicio;
import com.sura.arl.reproceso.accesodatos.NotificacionLimitePagoDao;
import com.sura.arl.reproceso.actores.notificacion.CalcularFechaLimitePago;
import com.sura.arl.reproceso.actores.notificacion.CalcularFechaLimitePago.SolicitudConsultaPolizasNotificacion;
import com.sura.arl.reproceso.modelo.TipoGeneracion;
import com.sura.arl.reproceso.modelo.notificacion.NotificacionLimitePago;
import com.sura.arl.reproceso.util.JSON;
import com.sura.arl.reproceso.util.VariablesEntorno;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.testkit.TestKit;
import scala.concurrent.duration.Duration;

@RunWith(MockitoJUnitRunner.class)
public class CalcularFechaLimitePagoUT {

    @Mock
    ApplicationContext context;

    @Mock
    NotificacionLimitePagoDao dao;

    @Mock
    VariablesEntorno entorno;

    @Mock
    ErroresProcesoServicio errorProcesoServicio;

    private static final Config config = ConfigFactory.empty();

    static ActorSystem system;
    static Materializer materializer;

    @BeforeClass
    public static void configuracion() {
        system = ActorSystem.create("sistemaPruebas", config);
        materializer = ActorMaterializer.create(system);
    }

    @Before
    public void inicializacion() {
        when(context.getBean(NotificacionLimitePagoDao.class)).thenReturn(dao);
        when(context.getBean(VariablesEntorno.class)).thenReturn(entorno);
    }

    @AfterClass
    public static void limpiar() {
        TestKit.shutdownActorSystem(system, Duration.create(5, TimeUnit.SECONDS), false);
        system = null;
    }

    @Test
    public void probarCalcularFechaLimitePago() {

        TestKit certificador = new TestKit(system);

        SolicitudConsultaPolizasNotificacion solicitud = new SolicitudConsultaPolizasNotificacion("201712",
                obtenerCoberturas(), TipoGeneracion.VENCIDA);

        Props props = Props.create(CalcularFechaLimitePago.class, context);
        ActorRef calcularFechaLimitePago = system.actorOf(props);
        calcularFechaLimitePago.tell(JSON.objetoToJson(solicitud), certificador.testActor());

        when(dao.consultaFechaLimitePago(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.of(Calendar.getInstance().getTime()));
        Mockito.doNothing().when(dao).ingresar(Mockito.anyListOf(NotificacionLimitePago.class));

        certificador.expectMsgClass(Done.class);
    }

    private List<Cobertura> obtenerCoberturas() {
        return Arrays.asList(new Cobertura("0000123", "201712"));
    }
}
