package com.projectmatch.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "recommend.mq.enabled", havingValue = "true")
public class RabbitRecommendEventBus implements RecommendEventBus {

    private final RabbitTemplate rabbitTemplate;

    public RabbitRecommendEventBus(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishCreated(Long projectId) {
        rabbitTemplate.convertAndSend(
                RabbitRecommendConfig.EXCHANGE,
                RabbitRecommendConfig.ROUTING_KEY,
                new ProjectCreatedEvent(projectId, "CREATED"));
    }
}
