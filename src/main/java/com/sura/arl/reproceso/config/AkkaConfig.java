package com.sura.arl.reproceso.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.management.AkkaManagement;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

@Configuration
public class AkkaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AkkaConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Profile({ "docker" })
    @Bean
    public Config configActorSystem() {

        Config appConfig = ConfigFactory.load("procesador-reproceso");
        Config config = ConfigFactory.empty().withFallback(appConfig);

        return config;
    }

    @Profile({ "default", "desarrollo", "laboratorio", "estadoCuenta" })
    @Bean
    public Config configActorSystemDesarrollador() {

        Config appConfig = ConfigFactory.load("procesador-reproceso-dev");
        Config config = ConfigFactory.empty().withFallback(appConfig);

        return config;
    }

    @Bean
    public ActorSystem actorSystem(Config config) {

        final String nombreSistema = config.getString("app.sistema");

        ActorSystem system = ActorSystem.create(nombreSistema, config);

        // Cuando el componente se ejecuta en un ambiente docker se coloca un
        // sleep aleatorio para que el inicio de varios componentes en paralelo
        // sean diferentes en tiempo de inicio.
        if (applicationContext.getEnvironment().acceptsProfiles("docker")) {
            AkkaManagement.get(system).start();
            ClusterBootstrap.get(system).start();
        }

        // Kamon.reconfigure(config);
        return system;
    }
    
    @Bean
    public Materializer materializer(ActorSystem system) {
        Materializer materializer = ActorMaterializer.create(system);
  
        return materializer;
    }

}
