package com.projectmatch.event;

import com.projectmatch.service.ProactiveMatchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "recommend.mq.enabled", havingValue = "true")
public class RabbitRecommendListener {

    private final ProactiveMatchService proactiveMatchService;

    public RabbitRecommendListener(ProactiveMatchService proactiveMatchService) {
        this.proactiveMatchService = proactiveMatchService;
    }

    @RabbitListener(queues = RabbitRecommendConfig.QUEUE)
    public void onProjectCreated(ProjectCreatedEvent event) {
        if (event == null || event.getProjectId() == null) {
            return;
        }
        proactiveMatchService.matchAndNotify(event.getProjectId());
    }
}
