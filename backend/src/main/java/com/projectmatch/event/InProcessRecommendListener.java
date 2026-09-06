package com.projectmatch.event;

import com.projectmatch.service.ProactiveMatchService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "recommend.mq.enabled", havingValue = "false", matchIfMissing = true)
public class InProcessRecommendListener {

    private final ProactiveMatchService proactiveMatchService;

    public InProcessRecommendListener(ProactiveMatchService proactiveMatchService) {
        this.proactiveMatchService = proactiveMatchService;
    }

    @Async("recommendExecutor")
    @EventListener
    public void onProjectCreated(ProjectCreatedEvent event) {
        if (event == null || event.getProjectId() == null) {
            return;
        }
        proactiveMatchService.matchAndNotify(event.getProjectId());
    }
}
