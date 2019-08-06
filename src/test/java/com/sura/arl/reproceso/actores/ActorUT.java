package com.sura.arl.reproceso.actores;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public abstract class ActorUT {

    @Mock
    ApplicationContext context;

    protected static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

}
