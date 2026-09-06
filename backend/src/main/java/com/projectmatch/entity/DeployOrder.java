package com.projectmatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("deploy_order")
public class DeployOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    private String contact;
    private String environmentNote;
    private Integer amount;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
