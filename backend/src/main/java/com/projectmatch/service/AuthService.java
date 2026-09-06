package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.BizException;
import com.projectmatch.dto.AuthResponse;
import com.projectmatch.dto.LoginRequest;
import com.projectmatch.dto.RegisterRequest;
import com.projectmatch.entity.SysUser;
import com.projectmatch.entity.UserProfile;
import com.projectmatch.mapper.SysUserMapper;
import com.projectmatch.mapper.UserProfileMapper;
import com.projectmatch.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserMapper userMapper, UserProfileMapper profileMapper,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setSkillLevel("junior");
        profileMapper.insert(profile);
        return tokenOf(user);
    }

    public AuthResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        return tokenOf(user);
    }

    public Map<String, Object> me(Long userId) {
        SysUser user = userMapper.selectById(userId);
        UserProfile profile = profileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        map.put("profile", profile);
        return map;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BizException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public UserProfile saveProfile(Long userId, UserProfile incoming) {
        UserProfile profile = profileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            incoming.setUserId(userId);
            profileMapper.insert(incoming);
            return incoming;
        }
        profile.setMajor(incoming.getMajor());
        profile.setSkillLevel(incoming.getSkillLevel());
        profile.setTechStack(incoming.getTechStack());
        profile.setInterests(incoming.getInterests());
        profile.setExpectedDifficulty(incoming.getExpectedDifficulty());
        profile.setExpectedDuration(incoming.getExpectedDuration());
        profile.setBio(incoming.getBio());
        profileMapper.updateById(profile);
        return profile;
    }

    private AuthResponse tokenOf(SysUser user) {
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
