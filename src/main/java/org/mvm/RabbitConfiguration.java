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
    public static final String TOPIC_EXCHANGE_POC = "poc.topic.exchange";

    public static final String DIRECT_EXCHANGE_POC = "poc.direct.exchange";

    public static final String TOPIC_EXCHANGE_QUEUE_POC = "poc.topic.exchange.queue";

    public static final String DIRECT_EXCHANGE_QUEUE_POC = "poc.direct.exchange.queue";

    public static final String TOPIC_EXCHANGE_ROUTING_KEY_POC = "poc.topic.exchange.routing-key";

    public static final String DIRECT_EXCHANGE_ROUTING_KEY_POC = "poc.direct.exchange.routing-key";

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
    public Exchange topicExchange() {
        return ExchangeBuilder.topicExchange(TOPIC_EXCHANGE_POC).build();
    }

    @Bean
    public Exchange directExchange() {
        return ExchangeBuilder.directExchange(DIRECT_EXCHANGE_POC).build();
    }

    @Bean
    public Queue topicExchangeQueue() {
        return QueueBuilder.nonDurable(TOPIC_EXCHANGE_QUEUE_POC).build();
    }

    @Bean
    public Queue directExchangeQueue() {
        return QueueBuilder.nonDurable(DIRECT_EXCHANGE_QUEUE_POC).build();
    }

    @Bean
    public Binding topicExchangeBinding(Queue topicExchangeQueue, TopicExchange exchange) {
        return BindingBuilder.bind(topicExchangeQueue).to(exchange).with(TOPIC_EXCHANGE_ROUTING_KEY_POC);
    }

    @Bean
    public Binding directExchangeBinding(Queue directExchangeQueue, DirectExchange exchange) {
        return BindingBuilder.bind(directExchangeQueue).to(exchange).with(DIRECT_EXCHANGE_ROUTING_KEY_POC);
    }
}
