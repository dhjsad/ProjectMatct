package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("project_resource")
public class ProjectResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String resourceType;
    private String title;
    private String content;
    private String accessType;
    private String fileName;
    private String filePath;
    private Long fileSize;
}
