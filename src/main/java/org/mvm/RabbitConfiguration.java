package org.mvm;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RabbitConfiguration {
    public final static String QUEUE_POC = "poc.queue";

    public final static String EXCHANGE_POC = "poc.topic.exchange";

    public final static String ROUTING_KEY_POC = "poc.rk.exchange";

    @Bean
    public ConnectionFactory defaultConnectionFactory(Environment environment) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(environment.getProperty("spring.rabbitmq.host"));
        connectionFactory.setPort(environment.getProperty("spring.rabbitmq.port", Integer.class));
        connectionFactory.setUsername(environment.getProperty("spring.rabbitmq.username"));
        connectionFactory.setPassword(environment.getProperty("spring.rabbitmq.password"));
        connectionFactory.setVirtualHost(environment.getProperty("spring.rabbitmq.virtual_host"));
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_POC).build();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.nonDurable(QUEUE_POC).build();
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_POC);
    }
}
