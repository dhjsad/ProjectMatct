package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.BizException;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.ProjectClaim;
import com.projectmatch.entity.ProjectResource;
import com.projectmatch.mapper.ProjectClaimMapper;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.ProjectResourceMapper;
import com.projectmatch.security.AuthUser;
import com.projectmatch.security.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DownloadService {

    private final ProjectMapper projectMapper;
    private final ProjectResourceMapper resourceMapper;
    private final ProjectClaimMapper claimMapper;
    private final FileStorageService fileStorageService;

    public DownloadService(ProjectMapper projectMapper, ProjectResourceMapper resourceMapper,
                           ProjectClaimMapper claimMapper, FileStorageService fileStorageService) {
        this.projectMapper = projectMapper;
        this.resourceMapper = resourceMapper;
        this.claimMapper = claimMapper;
        this.fileStorageService = fileStorageService;
    }

    public ResponseEntity<byte[]> tutorial(Long projectId) {
        Project project = requireProject(projectId);
        String markdown = tutorialMarkdown(project);
        return attachment(markdown.getBytes(StandardCharsets.UTF_8),
                safeName(project.getName()) + "-部署教程.md", MediaType.TEXT_PLAIN);
    }

    public ResponseEntity<byte[]> sourcePack(Long projectId, Long userId) {
        Project project = requireProject(projectId);
        assertClaimed(projectId, userId);
        try {
            byte[] zip = buildSourceZip(project);
            return attachment(zip, safeName(project.getName()) + "-源码学习包.zip", MediaType.APPLICATION_OCTET_STREAM);
        } catch (IOException e) {
            throw new BizException("打包源码失败");
        }
    }

    public ResponseEntity<byte[]> resourceFile(Long projectId, Long resourceId, Long userId) {
        requireProject(projectId);
        ProjectResource resource = resourceMapper.selectById(resourceId);
        if (resource == null || !projectId.equals(resource.getProjectId())) {
            throw new BizException("资源不存在");
        }
        if ("CLAIMED".equals(resource.getAccessType())) {
            assertClaimed(projectId, userId);
        }
        if (resource.getFilePath() == null || resource.getFilePath().isEmpty()) {
            if (resource.getContent() == null) {
                throw new BizException("该资料没有可下载文件");
            }
            return attachment(resource.getContent().getBytes(StandardCharsets.UTF_8),
                    (resource.getTitle() == null ? "resource" : resource.getTitle()) + ".txt",
                    MediaType.TEXT_PLAIN);
        }
        byte[] data = fileStorageService.read(resource.getFilePath());
        String name = resource.getFileName() == null ? "resource.bin" : resource.getFileName();
        return attachment(data, name, MediaType.APPLICATION_OCTET_STREAM);
    }

    private byte[] buildSourceZip(Project project) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(buffer);
        put(zip, "学习说明.md", learningNotice(project));
        put(zip, "部署教程.md", tutorialMarkdown(project));
        put(zip, "数据库设计.md", nvl(project.getDbDesign()));
        put(zip, "系统架构.md", nvl(project.getArchitecture()));
        put(zip, "示例代码.txt", nvl(project.getSampleCode()));

        List<ProjectResource> files = resourceMapper.selectList(new LambdaQueryWrapper<ProjectResource>()
                .eq(ProjectResource::getProjectId, project.getId())
                .eq(ProjectResource::getAccessType, "CLAIMED"));
        boolean hasUploadedSource = false;
        for (ProjectResource resource : files) {
            if (resource.getFilePath() != null && !resource.getFilePath().isEmpty()) {
                String folder = "SOURCE".equals(resource.getResourceType()) ? "源码/" : "附件/";
                zip.putNextEntry(new ZipEntry(folder + resource.getFileName()));
                zip.write(fileStorageService.read(resource.getFilePath()));
                zip.closeEntry();
                if ("SOURCE".equals(resource.getResourceType())) {
                    hasUploadedSource = true;
                }
            } else if (resource.getContent() != null) {
                put(zip, "资料/" + resource.getTitle() + ".txt", resource.getContent());
            }
        }
        if (!hasUploadedSource) {
            put(zip, "源码/待上传说明.txt",
                    "管理员尚未上传完整源码压缩包。当前学习包包含部署教程、库表说明和示例思路，供你对照自行实现。\n上传真实源码后，领取用户即可在此目录看到压缩包。");
        }
        zip.close();
        return buffer.toByteArray();
    }

    private void assertClaimed(Long projectId, Long userId) {
        AuthUser current = SecurityUtils.currentUser();
        if (current != null && "ADMIN".equals(current.getRole())) {
            return;
        }
        if (userId == null) {
            throw new BizException(401, "请先登录再下载源码");
        }
        Long count = claimMapper.selectCount(new LambdaQueryWrapper<ProjectClaim>()
                .eq(ProjectClaim::getUserId, userId)
                .eq(ProjectClaim::getProjectId, projectId));
        if (count == null || count == 0) {
            throw new BizException("请先领取该项目，再下载源码学习包");
        }
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        return project;
    }

    private static String tutorialMarkdown(Project project) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(project.getName()).append(" · 部署与开发教程\n\n");
        sb.append("> 本资料用于学习、本地运行和二次开发，请理解架构后自行实现，不要把他人成果直接作为作业提交。\n\n");
        sb.append("## 技术栈\n\n").append(nvl(project.getTechStack())).append("\n\n");
        sb.append("## 部署说明\n\n").append(nvl(project.getDeployGuide())).append("\n\n");
        sb.append("## 开发教程\n\n").append(nvl(project.getTutorial())).append("\n\n");
        sb.append("## 系统架构\n\n").append(nvl(project.getArchitecture())).append("\n\n");
        sb.append("## 数据库设计\n\n").append(nvl(project.getDbDesign())).append("\n");
        return sb.toString();
    }

    private static String learningNotice(Project project) {
        return "# " + project.getName() + " 学习包\n\n"
                + "领取后可下载部署教程和源码资料。\n\n"
                + "- 若管理员已上传源码压缩包，见 `源码/` 目录。\n"
                + "- 部署遇到环境问题，可在项目页申请付费远程部署协助。\n"
                + "- 请把本资料当作学习样本，完成属于你自己的实现与修改。\n";
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static ResponseEntity<byte[]> attachment(byte[] body, String filename, MediaType type) {
        String encoded = UriUtils.encode(filename, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentLength(body.length)
                .body(body);
    }

    private static String safeName(String name) {
        if (name == null || name.isEmpty()) {
            return "project";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static String nvl(String value) {
        return value == null || value.isEmpty() ? "（暂无）" : value;
    }
}
