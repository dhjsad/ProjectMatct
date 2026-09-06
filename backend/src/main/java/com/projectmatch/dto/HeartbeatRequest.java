package com.projectmatch.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class HeartbeatRequest {
    @NotBlank(message = "访客标识不能为空")
    @Size(max = 64)
    private String visitorId;

    @Size(max = 256)
    private String path;
}
