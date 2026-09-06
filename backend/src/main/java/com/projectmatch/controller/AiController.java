package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.dto.MatchResponse;
import com.projectmatch.dto.UserNeedProfile;
import com.projectmatch.dto.UserNeedRequest;
import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.MatchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final MatchService matchService;

    public AiController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/analyze")
    public ApiResult<UserNeedProfile> analyze(@RequestBody UserNeedRequest request) {
        return ApiResult.ok(matchService.analyze(request));
    }

    @PostMapping("/match")
    public ApiResult<MatchResponse> match(@RequestBody UserNeedRequest request) {
        return ApiResult.ok(matchService.match(request, SecurityUtils.currentUserId()));
    }
}
