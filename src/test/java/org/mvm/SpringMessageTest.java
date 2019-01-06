package org.mvm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mvm.RabbitConfiguration.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitConfiguration.class)
public class SpringMessageTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void convertAndSend_receive() {
        Message message = MessageBuilder.withBody("message body".getBytes())
                .setContentEncoding("text")
                .setHeader("header", "value")
                .build();

        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_POC, TOPIC_EXCHANGE_ROUTING_KEY_POC, message);
        Message received = rabbitTemplate.receive(TOPIC_EXCHANGE_QUEUE_POC);

        assertThat(received.getBody(), is("message body".getBytes()));
        assertThat(received.getMessageProperties().getContentEncoding(), is("text"));
        assertThat(received.getMessageProperties().getHeaders().get("header"), is("value"));
    }

    @Test
    public void convertAndSend_receiveAndConvert() {
        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_POC, TOPIC_EXCHANGE_ROUTING_KEY_POC, "test");
        String value = (String) rabbitTemplate.receiveAndConvert(TOPIC_EXCHANGE_QUEUE_POC);

        assertThat(value, is("test"));
    }
}
