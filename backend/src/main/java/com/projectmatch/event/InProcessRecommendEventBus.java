package com.projectmatch.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "recommend.mq.enabled", havingValue = "false", matchIfMissing = true)
public class InProcessRecommendEventBus implements RecommendEventBus {

    private final ApplicationEventPublisher publisher;

    public InProcessRecommendEventBus(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publishCreated(Long projectId) {
        publisher.publishEvent(new ProjectCreatedEvent(projectId, "CREATED"));
    }
}
