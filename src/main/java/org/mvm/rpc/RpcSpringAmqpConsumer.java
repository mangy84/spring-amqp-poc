package org.mvm.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static org.mvm.RabbitConfiguration.QUEUE_POC;

@Log4j2
@Component
public class RpcSpringAmqpConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RpcSpringAmqpConsumer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = QUEUE_POC)
    public void onMessage(Message<RpcMessage> message) throws JsonProcessingException {
        String replyTo = message.getHeaders().get(AmqpHeaders.REPLY_TO, String.class);
        String correlationId = message.getHeaders().get(AmqpHeaders.CORRELATION_ID, String.class);
        log.info("receiving request: " + message + " ; " + replyTo);

        this.rabbitTemplate.convertAndSend(replyTo, objectMapper.writeValueAsString(message.getPayload()),
                msg -> {
                    msg.getMessageProperties().setCorrelationId(correlationId);
                    return msg;
                }
        );
    }
}
