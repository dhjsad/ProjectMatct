package com.projectmatch.dto;

import lombok.Data;

@Data
public class UserNeedRequest {
    private String query;
    private String language;
    private String skillLevel;
    private String backend;
    private String frontend;
    private String difficulty;
    private String projectType;
    private String interests;
    private Integer durationDays;
    private String projectRequirement;
}
