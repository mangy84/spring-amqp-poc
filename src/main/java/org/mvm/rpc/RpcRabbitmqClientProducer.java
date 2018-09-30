package org.mvm.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

import static org.mvm.RabbitConfiguration.EXCHANGE_POC;
import static org.mvm.RabbitConfiguration.ROUTING_KEY_POC;

@Log4j2
public class RpcRabbitmqClientProducer {
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        ExceptionHandler exceptionHandler = new DefaultExceptionHandler() {
            @Override
            public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName) {
                log.error("> Error raised by: " + channel.getChannelNumber(), exception);
            }
        };
        connectionFactory.setExceptionHandler(exceptionHandler);

        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            String replyQueueName = "amq.rabbitmq.reply-to";
            String correlationId = UUID.randomUUID().toString();
            log.info("> connection is open: " + connection.isOpen() + " / CorrelationID: " + correlationId);

            RpcClient rpcClient = new RpcClient(channel, EXCHANGE_POC, ROUTING_KEY_POC);

            RpcMessage message = new RpcMessage("AMQP Java Client");
            ObjectMapper objectMapper = new ObjectMapper();

            log.info("> sending request...");
            RpcClient.Response response = rpcClient.doCall(
                    new AMQP.BasicProperties.Builder()
                            .contentType("application/json")
                            .deliveryMode(2)
                            .priority(1)
                            .appId("myApp")
                            .replyTo(replyQueueName)
                            .messageId(correlationId)
                            .correlationId(correlationId)
                            .type("myType")
                            .build(),
                    objectMapper.writeValueAsBytes(message));

            log.info("> receiving response:");
            log.info("> Properties: " + response.getProperties().toString());
            log.info("> Body: " + objectMapper.readValue(response.getBody(), RpcMessage.class));
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
