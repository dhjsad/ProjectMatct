package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_banner")
public class SiteBanner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String slotKey;
    private String content;
    private String fontFamily;
    private String fontSize;
    private String color;
    private String bgColor;
    private Integer enabledFlag;
    private LocalDateTime updateTime;
}
