package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.projectmatch.common.BizException;
import com.projectmatch.dto.PurchaseRequest;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectClaim;
import com.projectmatch.entity.ProjectResource;
import com.projectmatch.entity.SiteMessage;
import com.projectmatch.entity.SysUser;
import com.projectmatch.event.RecommendEventBus;
import com.projectmatch.mapper.ProjectClaimMapper;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.ProjectResourceMapper;
import com.projectmatch.mapper.SiteMessageMapper;
import com.projectmatch.mapper.SysUserMapper;
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
    private final SysUserMapper userMapper;
    private final RecommendEventBus recommendEventBus;

    public ProjectService(ProjectMapper projectMapper, ProjectResourceMapper resourceMapper,
                          ProjectClaimMapper claimMapper, SiteMessageMapper messageMapper,
                          SysUserMapper userMapper, RecommendEventBus recommendEventBus) {
        this.projectMapper = projectMapper;
        this.resourceMapper = resourceMapper;
        this.claimMapper = claimMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
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
        boolean sold = project.getRemainingCount() == null || project.getRemainingCount() <= 0;
        map.put("sold", sold);
        map.put("claimable", !sold);
        map.put("hasSourceFile", sourceFiles != null && sourceFiles > 0);
        map.put("deployServiceEnabled", project.getDeployServiceEnabled() == null || project.getDeployServiceEnabled() == 1);
        ProjectClaim myClaim = null;
        if (userId != null && claimed) {
            myClaim = claimMapper.selectOne(new LambdaQueryWrapper<ProjectClaim>()
                    .eq(ProjectClaim::getUserId, userId)
                    .eq(ProjectClaim::getProjectId, id)
                    .last("LIMIT 1"));
        }
        map.put("myClaim", myClaim);
        return map;
    }

    @Transactional
    public void claim(Long projectId, Long userId) {
        PurchaseRequest request = new PurchaseRequest();
        request.setDeliveryType("SELF_DOWNLOAD");
        purchase(projectId, userId, request);
    }

    @Transactional
    public ProjectClaim purchase(Long projectId, Long userId, PurchaseRequest request) {
        Long already = claimMapper.selectCount(new LambdaQueryWrapper<ProjectClaim>()
                .eq(ProjectClaim::getUserId, userId)
                .eq(ProjectClaim::getProjectId, projectId));
        if (already != null && already > 0) {
            throw new BizException("你已经购买过该作品");
        }
        int updated = projectMapper.markSold(projectId);
        if (updated == 0) {
            throw new BizException("该作品已售出，保证独一无二");
        }
        Project project = projectMapper.selectById(projectId);
        String delivery = request != null && StringUtils.hasText(request.getDeliveryType())
                ? request.getDeliveryType().trim().toUpperCase()
                : "SELF_DOWNLOAD";
        if (!"SELF_DOWNLOAD".equals(delivery) && !"SERVICE".equals(delivery)) {
            throw new BizException("请选择发货方式");
        }
        int sale = project.getSalePrice() == null ? 399 : project.getSalePrice();
        int guide = 0;
        if ("SERVICE".equals(delivery)) {
            guide = project.getGuidePrice() == null ? 299 : project.getGuidePrice();
            if (!StringUtils.hasText(request.getContact())) {
                throw new BizException("客服发货请填写联系方式");
            }
        }
        ProjectClaim claim = new ProjectClaim();
        claim.setUserId(userId);
        claim.setProjectId(projectId);
        claim.setDeliveryType(delivery);
        claim.setContact(request == null ? null : request.getContact());
        claim.setAmount(sale);
        claim.setGuideAmount(guide);
        claim.setClaimTime(LocalDateTime.now());
        claimMapper.insert(claim);

        if ("SELF_DOWNLOAD".equals(delivery)) {
            notify(userId, projectId, "PURCHASE", "购买成功，可立即下载",
                    "你已买下「" + project.getName() + "」（¥" + sale + "）。请自行下载源码，作品已下架，不再二次售卖。");
        } else {
            notify(userId, projectId, "PURCHASE", "购买成功，客服将发货",
                    "你已买下「" + project.getName() + "」（作品 ¥" + sale + " + 技术指导 ¥" + guide
                            + "）。客服会按联系方式发送源码并提供付费指导。");
            notifyAdmins(userId, project, claim);
        }
        return claim;
    }

    @Transactional
    public List<ProjectClaim> checkout(Long userId, List<PurchaseRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException("购物车是空的");
        }
        List<ProjectClaim> result = new java.util.ArrayList<ProjectClaim>();
        for (PurchaseRequest item : items) {
            if (item == null || item.getProjectId() == null) {
                throw new BizException("购物车项目无效");
            }
            result.add(purchase(item.getProjectId(), userId, item));
        }
        return result;
    }

    private void notifyAdmins(Long userId, Project project, ProjectClaim claim) {
        List<SysUser> admins = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "ADMIN"));
        String contact = StringUtils.hasText(claim.getContact()) ? claim.getContact() : "未填写";
        for (SysUser admin : admins) {
            notify(admin.getId(), project.getId(), "FULFILL", "新的客服发货单",
                    "用户 #" + userId + " 购买「" + project.getName() + "」，请发送源码并安排技术指导（¥"
                            + claim.getGuideAmount() + "），联系方式：" + contact);
        }
    }

    private void notify(Long userId, Long projectId, String type, String title, String content) {
        SiteMessage message = new SiteMessage();
        message.setUserId(userId);
        message.setProjectId(projectId);
        message.setMsgType(type);
        message.setTitle(title);
        message.setContent(content);
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
        if (project.getSalePrice() == null) {
            project.setSalePrice(399);
        }
        if (project.getDeployPrice() == null) {
            project.setDeployPrice(199);
        }
        if (project.getGuidePrice() == null) {
            project.setGuidePrice(299);
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
