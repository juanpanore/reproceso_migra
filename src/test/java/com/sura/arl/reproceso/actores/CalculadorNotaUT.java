package com.sura.arl.reproceso.actores;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.sura.arl.reproceso.modelo.PlanillaLegalizada;
import com.sura.arl.reproceso.servicios.RenesServicio;
import com.sura.arl.reproceso.servicios.ReprocesoCargaServicio;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class CalculadorNotaUT extends ActorUT {

    @Mock
    ApplicationContext context;

    @Mock
    ReprocesoCargaServicio correccionesEsperadasServicio;

    @Mock
    RenesServicio renesServicio;

    PlanillaLegalizada planillaLegalizada = PlanillaLegalizada.builder().numeroFormulario(1L).build();

    @Before
    public void inicializacion() {
        when(context.getBean(ReprocesoCargaServicio.class)).thenReturn(correccionesEsperadasServicio);
        when(context.getBean(RenesServicio.class)).thenReturn(renesServicio);
    }

//    @Test
//    public void probarCalculoNotaExitoso() {
//
//        new TestKit(system) {
//            {
//                final Props props = Props.create(CalculadorNota.class, context);
//                final ActorRef subject = system.actorOf(props);
//
//                // Se envia mensaje
//                subject.tell(planillaLegalizada, getRef());
//
//                // Verificacion de comportamiento y resultado
//                expectMsg(duration("3 seconds"), "OK");
//
//                InOrder order = Mockito.inOrder(correccionesEsperadasServicio, renesServicio);
//
//                order.verify(correccionesEsperadasServicio).ejecutar();
//            }
//        };
//    }

}
