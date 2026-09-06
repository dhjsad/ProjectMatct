package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("site_visit")
public class SiteVisit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String visitorId;
    private Long userId;
    private String username;
    private String ip;
    private String path;
    private String userAgent;
    private LocalDate visitDate;
    private Integer pageViews;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
}
