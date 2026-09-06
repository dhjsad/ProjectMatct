package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String projectType;
    private String category;
    private String techStack;
    private String difficulty;
    private Integer estimatedDuration;
    private String suitableFor;
    private String modules;
    private String architecture;
    private String dbDesign;
    private String deployGuide;
    private String sampleCode;
    private String tutorial;
    private Integer remainingCount;
    private Integer deployPrice;
    private Integer deployServiceEnabled;
    private String status;
    private LocalDateTime createTime;
}
