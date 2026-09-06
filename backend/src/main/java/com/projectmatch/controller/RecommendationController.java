package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.dto.RecommendationView;
import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.RecommendationQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationQueryService recommendationQueryService;

    public RecommendationController(RecommendationQueryService recommendationQueryService) {
        this.recommendationQueryService = recommendationQueryService;
    }

    @GetMapping
    public ApiResult<List<RecommendationView>> mine() {
        return ApiResult.ok(recommendationQueryService.mine(SecurityUtils.currentUserId()));
    }
}
