package com.projectmatch.event;

import java.io.Serializable;

public class ProjectCreatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;
    private String source;

    public ProjectCreatedEvent() {
    }

    public ProjectCreatedEvent(Long projectId, String source) {
        this.projectId = projectId;
        this.source = source;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
