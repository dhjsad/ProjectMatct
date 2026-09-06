package com.projectmatch.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "recommend.mq.enabled", havingValue = "true")
public class RabbitRecommendConfig {

    public static final String EXCHANGE = "projectmatch.direct";
    public static final String QUEUE = "project.created";
    public static final String ROUTING_KEY = "project.created";

    @Bean
    public DirectExchange projectMatchExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue projectCreatedQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding projectCreatedBinding(Queue projectCreatedQueue, DirectExchange projectMatchExchange) {
        return BindingBuilder.bind(projectCreatedQueue).to(projectMatchExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
