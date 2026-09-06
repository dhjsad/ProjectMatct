package com.projectmatch.dto;

import com.projectmatch.entity.Project;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MatchResult {
    private Project project;
    private int matchScore;
    private int techScore;
    private int difficultyScore;
    private int typeScore;
    private int interestScore;
    private int durationScore;
    private List<String> reasons = new ArrayList<String>();
}
