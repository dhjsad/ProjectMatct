package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.dto.RecommendationView;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.UserRecommendation;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.UserRecommendationMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationQueryService {

    private final UserRecommendationMapper recommendationMapper;
    private final ProjectMapper projectMapper;

    public RecommendationQueryService(UserRecommendationMapper recommendationMapper, ProjectMapper projectMapper) {
        this.recommendationMapper = recommendationMapper;
        this.projectMapper = projectMapper;
    }

    public List<RecommendationView> mine(Long userId) {
        List<UserRecommendation> rows = recommendationMapper.selectList(new LambdaQueryWrapper<UserRecommendation>()
                .eq(UserRecommendation::getUserId, userId)
                .eq(UserRecommendation::getStatus, "PUSHED")
                .orderByDesc(UserRecommendation::getCreateTime));
        List<RecommendationView> views = new ArrayList<RecommendationView>();
        for (UserRecommendation row : rows) {
            Project project = projectMapper.selectById(row.getProjectId());
            views.add(RecommendationView.from(row, project));
        }
        return views;
    }

    public List<UserRecommendation> all() {
        return recommendationMapper.selectList(new LambdaQueryWrapper<UserRecommendation>()
                .orderByDesc(UserRecommendation::getCreateTime));
    }
}
