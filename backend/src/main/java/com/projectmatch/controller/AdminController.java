package com.projectmatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projectmatch.common.ApiResult;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectClaim;
import com.projectmatch.entity.SysUser;
import com.projectmatch.entity.UserProfile;
import com.projectmatch.entity.UserRecommendation;
import com.projectmatch.mapper.ProjectClaimMapper;
import com.projectmatch.mapper.SysUserMapper;
import com.projectmatch.mapper.UserProfileMapper;
import com.projectmatch.entity.DeployOrder;
import com.projectmatch.entity.ProjectResource;
import com.projectmatch.dto.AdminResetPasswordRequest;
import com.projectmatch.service.AuthService;
import com.projectmatch.service.DeployOrderService;
import com.projectmatch.service.PresenceService;
import com.projectmatch.service.ProjectService;
import com.projectmatch.service.ProactiveMatchService;
import com.projectmatch.service.RecommendationQueryService;
import com.projectmatch.service.ResourceAdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProjectService projectService;
    private final SysUserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final ProjectClaimMapper claimMapper;
    private final ProactiveMatchService proactiveMatchService;
    private final RecommendationQueryService recommendationQueryService;
    private final ResourceAdminService resourceAdminService;
    private final DeployOrderService deployOrderService;
    private final PresenceService presenceService;
    private final AuthService authService;

    public AdminController(ProjectService projectService, SysUserMapper userMapper,
                           UserProfileMapper profileMapper, ProjectClaimMapper claimMapper,
                           ProactiveMatchService proactiveMatchService,
                           RecommendationQueryService recommendationQueryService,
                           ResourceAdminService resourceAdminService,
                           DeployOrderService deployOrderService,
                           PresenceService presenceService,
                           AuthService authService) {
        this.projectService = projectService;
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.claimMapper = claimMapper;
        this.proactiveMatchService = proactiveMatchService;
        this.recommendationQueryService = recommendationQueryService;
        this.resourceAdminService = resourceAdminService;
        this.deployOrderService = deployOrderService;
        this.presenceService = presenceService;
        this.authService = authService;
    }

    @GetMapping("/projects")
    public ApiResult<Page<Project>> projects(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        return ApiResult.ok(projectService.adminPage(page, size));
    }

    @PostMapping("/projects")
    public ApiResult<Project> create(@RequestBody Project project) {
        project.setId(null);
        return ApiResult.ok(projectService.save(project));
    }

    @PutMapping("/projects/{id}")
    public ApiResult<Project> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return ApiResult.ok(projectService.save(project));
    }

    @DeleteMapping("/projects/{id}")
    public ApiResult<String> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ApiResult.ok("deleted");
    }

    @GetMapping("/users")
    public ApiResult<List<SysUser>> users() {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreateTime));
        for (SysUser user : users) {
            user.setPassword(null);
        }
        return ApiResult.ok(users);
    }

    @GetMapping("/users/{id}/profile")
    public ApiResult<Map<String, Object>> userProfile(@PathVariable Long id) {
        Map<String, Object> map = new HashMap<String, Object>();
        SysUser user = userMapper.selectById(id);
        if (user != null) user.setPassword(null);
        map.put("user", user);
        map.put("profile", profileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, id)));
        map.put("claims", claimMapper.selectList(new LambdaQueryWrapper<ProjectClaim>().eq(ProjectClaim::getUserId, id)));
        return ApiResult.ok(map);
    }

    @PutMapping("/users/{id}/password")
    public ApiResult<String> resetPassword(@PathVariable Long id, @Valid @RequestBody AdminResetPasswordRequest request) {
        authService.resetPassword(id, request.getNewPassword());
        return ApiResult.ok("密码已重置");
    }

    @GetMapping("/claims")
    public ApiResult<List<ProjectClaim>> claims() {
        return ApiResult.ok(claimMapper.selectList(new LambdaQueryWrapper<ProjectClaim>().orderByDesc(ProjectClaim::getClaimTime)));
    }

    @GetMapping("/recommendations")
    public ApiResult<List<UserRecommendation>> recommendations() {
        return ApiResult.ok(recommendationQueryService.all());
    }

    @PostMapping("/projects/{id}/push")
    public ApiResult<Map<String, Object>> push(@PathVariable Long id) {
        int notified = proactiveMatchService.matchAndNotify(id);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("notified", notified);
        map.put("message", "已向 " + notified + " 位匹配用户发送站内推荐");
        return ApiResult.ok(map);
    }

    @GetMapping("/projects/{id}/resources")
    public ApiResult<List<ProjectResource>> resources(@PathVariable Long id) {
        return ApiResult.ok(resourceAdminService.list(id));
    }

    @PostMapping("/projects/{id}/resources")
    public ApiResult<ProjectResource> upload(@PathVariable Long id,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(defaultValue = "SOURCE") String resourceType,
                                             @RequestParam(defaultValue = "CLAIMED") String accessType) {
        return ApiResult.ok(resourceAdminService.upload(id, title, resourceType, accessType, file));
    }

    @DeleteMapping("/projects/{id}/resources/{resourceId}")
    public ApiResult<String> deleteResource(@PathVariable Long id, @PathVariable Long resourceId) {
        resourceAdminService.delete(id, resourceId);
        return ApiResult.ok("deleted");
    }

    @GetMapping("/deploy-orders")
    public ApiResult<List<DeployOrder>> deployOrders() {
        return ApiResult.ok(deployOrderService.all());
    }

    @PostMapping("/deploy-orders/{id}/status")
    public ApiResult<DeployOrder> deployStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResult.ok(deployOrderService.updateStatus(id, status));
    }

    @GetMapping("/online")
    public ApiResult<Map<String, Object>> online() {
        return ApiResult.ok(presenceService.stats());
    }
}
