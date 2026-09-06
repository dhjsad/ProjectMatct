package com.projectmatch.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度为 3-32")
    private String username;
    private String email;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码至少 6 位")
    private String password;
}
