package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_profile")
public class UserProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String major;
    private String skillLevel;
    private String techStack;
    private String interests;
    private String expectedDifficulty;
    private Integer expectedDuration;
    private String bio;
}
