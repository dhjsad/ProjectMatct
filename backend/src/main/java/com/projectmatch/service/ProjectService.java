package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projectmatch.common.BizException;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectClaim;
import com.projectmatch.entity.ProjectResource;
import com.projectmatch.entity.SiteMessage;
import com.projectmatch.event.RecommendEventBus;
import com.projectmatch.mapper.ProjectClaimMapper;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.ProjectResourceMapper;
import com.projectmatch.mapper.SiteMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectResourceMapper resourceMapper;
    private final ProjectClaimMapper claimMapper;
    private final SiteMessageMapper messageMapper;
    private final RecommendEventBus recommendEventBus;

    public ProjectService(ProjectMapper projectMapper, ProjectResourceMapper resourceMapper,
                          ProjectClaimMapper claimMapper, SiteMessageMapper messageMapper,
                          RecommendEventBus recommendEventBus) {
        this.projectMapper = projectMapper;
        this.resourceMapper = resourceMapper;
        this.claimMapper = claimMapper;
        this.messageMapper = messageMapper;
        this.recommendEventBus = recommendEventBus;
    }

    public Page<Project> search(String keyword, String category, String difficulty, String projectType, long page, long size) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, "PUBLISHED")
                .eq(StringUtils.hasText(category), Project::getCategory, category)
                .eq(StringUtils.hasText(difficulty), Project::getDifficulty, difficulty)
                .eq(StringUtils.hasText(projectType), Project::getProjectType, projectType)
                .and(StringUtils.hasText(keyword), w -> w.like(Project::getName, keyword)
                        .or().like(Project::getDescription, keyword)
                        .or().like(Project::getTechStack, keyword)
                        .or().like(Project::getModules, keyword))
                .orderByDesc(Project::getCreateTime);
        return projectMapper.selectPage(new Page<Project>(page, size), wrapper);
    }

    public Map<String, Object> detail(Long id, Long userId) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        List<ProjectResource> publicResources = resourceMapper.selectList(
                new LambdaQueryWrapper<ProjectResource>()
                        .eq(ProjectResource::getProjectId, id)
                        .eq(ProjectResource::getAccessType, "PUBLIC"));
        boolean claimed = false;
        List<ProjectResource> claimedResources = java.util.Collections.emptyList();
        if (userId != null) {
            claimed = claimMapper.selectCount(new LambdaQueryWrapper<ProjectClaim>()
                    .eq(ProjectClaim::getUserId, userId)
                    .eq(ProjectClaim::getProjectId, id)) > 0;
            if (claimed) {
                claimedResources = resourceMapper.selectList(
                        new LambdaQueryWrapper<ProjectResource>()
                                .eq(ProjectResource::getProjectId, id)
                                .eq(ProjectResource::getAccessType, "CLAIMED"));
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("project", project);
        map.put("publicResources", publicResources);
        map.put("claimedResources", claimedResources);
        Long sourceFiles = resourceMapper.selectCount(new LambdaQueryWrapper<ProjectResource>()
                .eq(ProjectResource::getProjectId, id)
                .eq(ProjectResource::getResourceType, "SOURCE")
                .isNotNull(ProjectResource::getFilePath));
        map.put("claimedByMe", claimed);
        map.put("claimable", project.getRemainingCount() != null && project.getRemainingCount() > 0);
        map.put("hasSourceFile", sourceFiles != null && sourceFiles > 0);
        map.put("deployServiceEnabled", project.getDeployServiceEnabled() == null || project.getDeployServiceEnabled() == 1);
        return map;
    }

    @Transactional
    public void claim(Long projectId, Long userId) {
        Long already = claimMapper.selectCount(new LambdaQueryWrapper<ProjectClaim>()
                .eq(ProjectClaim::getUserId, userId)
                .eq(ProjectClaim::getProjectId, projectId));
        if (already != null && already > 0) {
            throw new BizException("你已经领取过该项目");
        }
        int updated = projectMapper.decreaseRemaining(projectId);
        if (updated == 0) {
            throw new BizException("领取名额已满，完整资源不再开放新的领取资格");
        }
        ProjectClaim claim = new ProjectClaim();
        claim.setUserId(userId);
        claim.setProjectId(projectId);
        claim.setClaimTime(LocalDateTime.now());
        claimMapper.insert(claim);

        Project project = projectMapper.selectById(projectId);
        SiteMessage message = new SiteMessage();
        message.setUserId(userId);
        message.setProjectId(projectId);
        message.setMsgType("CLAIM");
        message.setTitle("领取成功");
        message.setContent("你已成功领取「" + project.getName() + "」的学习资源。请把它作为学习与二次开发的起点，而不是直接提交的作业成果。");
        message.setReadFlag(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    public List<ProjectClaim> myClaims(Long userId) {
        return claimMapper.selectList(new LambdaQueryWrapper<ProjectClaim>()
                .eq(ProjectClaim::getUserId, userId)
                .orderByDesc(ProjectClaim::getClaimTime));
    }

    public Project save(Project project) {
        if (project.getStatus() == null) {
            project.setStatus("PUBLISHED");
        }
        if (project.getRemainingCount() == null) {
            project.setRemainingCount(1);
        }
        if (project.getDeployPrice() == null) {
            project.setDeployPrice(199);
        }
        if (project.getDeployServiceEnabled() == null) {
            project.setDeployServiceEnabled(1);
        }
        boolean created = project.getId() == null;
        if (created) {
            project.setCreateTime(LocalDateTime.now());
            projectMapper.insert(project);
            if ("PUBLISHED".equals(project.getStatus())) {
                recommendEventBus.publishCreated(project.getId());
            }
        } else {
            projectMapper.updateById(project);
        }
        return project;
    }

    public void delete(Long id) {
        projectMapper.deleteById(id);
    }

    public Page<Project> adminPage(long page, long size) {
        return projectMapper.selectPage(new Page<Project>(page, size),
                new LambdaQueryWrapper<Project>().orderByDesc(Project::getCreateTime));
    }
}
