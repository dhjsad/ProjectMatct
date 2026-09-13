package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("custom_order")
public class CustomOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String contactName;
    private String contact;
    private String company;
    private String title;
    private String requirement;
    private String budget;
    private String status;
    private LocalDateTime createTime;
}
