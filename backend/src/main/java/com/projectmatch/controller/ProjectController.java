package com.projectmatch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projectmatch.common.ApiResult;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectClaim;
import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResult<Page<Project>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String difficulty,
                                         @RequestParam(required = false) String projectType,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "8") long size) {
        return ApiResult.ok(projectService.search(keyword, category, difficulty, projectType, page, size));
    }

    @GetMapping("/mine/claims")
    public ApiResult<List<ProjectClaim>> myClaims() {
        return ApiResult.ok(projectService.myClaims(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.ok(projectService.detail(id, SecurityUtils.currentUserId()));
    }

    @PostMapping("/{id}/claim")
    public ApiResult<String> claim(@PathVariable Long id) {
        projectService.claim(id, SecurityUtils.currentUserId());
        return ApiResult.ok("领取成功", "ok");
    }
}
