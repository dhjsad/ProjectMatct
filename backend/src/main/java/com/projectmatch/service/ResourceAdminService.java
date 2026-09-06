package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.BizException;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectResource;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.ProjectResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResourceAdminService {

    private final ProjectMapper projectMapper;
    private final ProjectResourceMapper resourceMapper;
    private final FileStorageService fileStorageService;

    public ResourceAdminService(ProjectMapper projectMapper, ProjectResourceMapper resourceMapper,
                                FileStorageService fileStorageService) {
        this.projectMapper = projectMapper;
        this.resourceMapper = resourceMapper;
        this.fileStorageService = fileStorageService;
    }

    public List<ProjectResource> list(Long projectId) {
        requireProject(projectId);
        return resourceMapper.selectList(new LambdaQueryWrapper<ProjectResource>()
                .eq(ProjectResource::getProjectId, projectId)
                .orderByDesc(ProjectResource::getId));
    }

    public ProjectResource upload(Long projectId, String title, String resourceType, String accessType, MultipartFile file) {
        requireProject(projectId);
        FileStorageService.StoredFile stored = fileStorageService.store(projectId, file);
        ProjectResource resource = new ProjectResource();
        resource.setProjectId(projectId);
        resource.setTitle(title == null || title.isEmpty() ? stored.getFileName() : title);
        resource.setResourceType(resourceType == null || resourceType.isEmpty() ? "SOURCE" : resourceType);
        resource.setAccessType(accessType == null || accessType.isEmpty() ? "CLAIMED" : accessType);
        resource.setFileName(stored.getFileName());
        resource.setFilePath(stored.getFilePath());
        resource.setFileSize(stored.getFileSize());
        resourceMapper.insert(resource);
        return resource;
    }

    public void delete(Long projectId, Long resourceId) {
        ProjectResource resource = resourceMapper.selectById(resourceId);
        if (resource == null || !projectId.equals(resource.getProjectId())) {
            throw new BizException("资源不存在");
        }
        fileStorageService.deleteQuietly(resource.getFilePath());
        resourceMapper.deleteById(resourceId);
    }

    private void requireProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在");
        }
    }
}
