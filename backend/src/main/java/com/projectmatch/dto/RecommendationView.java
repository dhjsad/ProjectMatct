package com.projectmatch.dto;

import com.projectmatch.entity.Project;
import com.projectmatch.entity.UserRecommendation;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class RecommendationView {
    private Long id;
    private Long projectId;
    private String projectName;
    private String description;
    private String techStack;
    private Integer remainingCount;
    private Integer estimatedDuration;
    private BigDecimal matchScore;
    private List<String> reasons = new ArrayList<String>();
    private String status;
    private LocalDateTime createTime;

    public static RecommendationView from(UserRecommendation rec, Project project) {
        RecommendationView view = new RecommendationView();
        view.setId(rec.getId());
        view.setProjectId(rec.getProjectId());
        view.setMatchScore(rec.getMatchScore());
        view.setStatus(rec.getStatus());
        view.setCreateTime(rec.getCreateTime());
        if (rec.getRecommendReason() != null && !rec.getRecommendReason().isEmpty()) {
            view.setReasons(Arrays.asList(rec.getRecommendReason().split("\\n")));
        }
        if (project != null) {
            view.setProjectName(project.getName());
            view.setDescription(project.getDescription());
            view.setTechStack(project.getTechStack());
            view.setRemainingCount(project.getRemainingCount());
            view.setEstimatedDuration(project.getEstimatedDuration());
        }
        return view;
    }
}
