package com.projectmatch.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserNeedProfile {
    private String language;
    private String skillLevel;
    private List<String> backend = new ArrayList<String>();
    private List<String> frontend = new ArrayList<String>();
    private List<String> extraStack = new ArrayList<String>();
    private String difficulty;
    private String projectType;
    private String interests;
    private Integer durationDays;
    private String projectRequirement;
    private String rawQuery;
}
