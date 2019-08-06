package com.sura.arl.reproceso.config;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource(name = "queries", value = "classpath:queries.xml")
@PropertySource(name = "queriesEstadoCuenta", value = "classpath:estadocuenta.queries.xml")
@PropertySource(name = "queriesIntegrador", value = "classpath:integrador.queries.xml")
@PropertySource(name = "parametros", value = "classpath:parametros.properties")
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@ComponentScan(basePackages = "com.sura.arl")
public class AplicacionConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    Environment env;

    @PostConstruct
    public void post() {
        // Se cargan los parametros del archivos parametros.properties como
        // variables de sistema.
        AbstractApplicationContext applicationcontext = (AbstractApplicationContext) this.applicationContext;
        Properties props = (Properties) applicationcontext.getEnvironment().getPropertySources().get("parametros")
                .getSource();
        Map<String, Object> propsSystem = applicationcontext.getEnvironment().getSystemProperties();

        props.forEach((k, v) -> {
            if (!propsSystem.containsKey(k)) {
                System.setProperty((String) k, (String) v);
            }
        });
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Integer.valueOf(env.getRequiredProperty("executors.size")));
    }

}
