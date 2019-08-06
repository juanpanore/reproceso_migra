package com.sura.arl.reproceso.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.TopologyRecoveryException;
import com.sura.arl.reproceso.util.VariablesEntorno;

import akka.stream.alpakka.amqp.AmqpConnectionDetails;
import akka.stream.alpakka.amqp.AmqpCredentials;

@Configuration
//@Profile("estadoCuenta")
public class BrokerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerConfig.class);

    @Autowired
    private VariablesEntorno entorno;

    @Bean
    public AmqpCredentials credentials() {
        String userBroker = entorno.getValor("broker.user");
        String passBroker = entorno.getValor("broker.password");

        return AmqpCredentials.create(userBroker, passBroker);
    }

    @Bean
    public AmqpConnectionDetails connection(AmqpCredentials credentials) {

        String hostBroker = entorno.getValor("broker.host");
        String portBroker = entorno.getValor("broker.port");
        String vhBroker = entorno.getValor("broker.virtualhost");

        AmqpConnectionDetails connDetails = AmqpConnectionDetails.create(hostBroker, Integer.parseInt(portBroker))
                .withCredentials(credentials).withVirtualHost(vhBroker)
                .withExceptionHandler(new ControlExcepcionBroker()).withConnectionTimeout(5000);

        return connDetails;
    }

    public static class ControlExcepcionBroker implements ExceptionHandler {

        @Override
        public void handleUnexpectedConnectionDriverException(Connection conn, Throwable exception) {
            LOG.error(exception.getMessage(), exception);

        }

        @Override
        public void handleReturnListenerException(Channel channel, Throwable exception) {
            LOG.error(exception.getMessage(), exception);

        }

        @Override
        public void handleConfirmListenerException(Channel channel, Throwable exception) {
            LOG.error(exception.getMessage(), exception);
        }

        @Override
        public void handleBlockedListenerException(Connection connection, Throwable exception) {
            LOG.error(exception.getMessage(), exception);
        }

        @Override
        public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag,
                String methodName) {
            LOG.error(exception.getMessage(), exception);
        }

        @Override
        public void handleConnectionRecoveryException(Connection conn, Throwable exception) {
            LOG.error(exception.getMessage(), exception);
        }

        @Override
        public void handleChannelRecoveryException(Channel ch, Throwable exception) {
            LOG.error(exception.getMessage(), exception);
        }

        @Override
        public void handleTopologyRecoveryException(Connection conn, Channel ch, TopologyRecoveryException exception) {
            LOG.error(exception.getMessage(), exception);
        }

        @Override
        public void handleFlowListenerException(Channel channel, Throwable exception) {
            // TODO Auto-generated method stub

        }
    }

}
