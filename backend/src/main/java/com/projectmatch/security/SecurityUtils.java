package com.projectmatch.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static AuthUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser)) {
            return null;
        }
        return (AuthUser) auth.getPrincipal();
    }

    public static Long currentUserId() {
        AuthUser user = currentUser();
        return user == null ? null : user.getId();
    }
}
