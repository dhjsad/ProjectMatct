package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.dto.AuthResponse;
import com.projectmatch.dto.ChangePasswordRequest;
import com.projectmatch.dto.LoginRequest;
import com.projectmatch.dto.RegisterRequest;
import com.projectmatch.entity.UserProfile;
import com.projectmatch.security.AuthUser;
import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @org.springframework.web.bind.annotation.PostMapping("/register")
    public ApiResult<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.ok(authService.register(request));
    }

    @org.springframework.web.bind.annotation.PostMapping("/login")
    public ApiResult<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResult<Map<String, Object>> me(@AuthenticationPrincipal AuthUser user) {
        return ApiResult.ok(authService.me(user.getId()));
    }

    @PutMapping("/profile")
    public ApiResult<UserProfile> profile(@RequestBody UserProfile profile) {
        return ApiResult.ok(authService.saveProfile(SecurityUtils.currentUserId(), profile));
    }

    @PutMapping("/password")
    public ApiResult<String> password(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.currentUserId(), request.getOldPassword(), request.getNewPassword());
        return ApiResult.ok("密码已更新");
    }
}
