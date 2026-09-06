package com.projectmatch.ai;

import com.projectmatch.dto.UserNeedProfile;
import com.projectmatch.entity.UserProfile;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProfileNeedMapper {
    private ProfileNeedMapper() {
    }

    public static UserNeedProfile from(UserProfile profile) {
        UserNeedProfile need = new UserNeedProfile();
        if (profile == null) {
            return need;
        }
        need.setSkillLevel(profile.getSkillLevel());
        need.setDifficulty(profile.getExpectedDifficulty());
        need.setInterests(profile.getInterests());
        need.setDurationDays(profile.getExpectedDuration());
        need.setRawQuery(profile.getBio());
        List<String> backend = new ArrayList<String>();
        List<String> frontend = new ArrayList<String>();
        List<String> extra = new ArrayList<String>();
        for (String token : split(profile.getTechStack())) {
            String low = token.toLowerCase(Locale.ROOT);
            if (need.getLanguage() == null) {
                if (low.contains("java")) {
                    need.setLanguage("Java");
                } else if (low.contains("python")) {
                    need.setLanguage("Python");
                }
            }
            if (low.contains("vue") || low.contains("react") || low.contains("小程序") || low.contains("thymeleaf") || low.contains("jsp")) {
                frontend.add(token);
            } else if (low.contains("mysql") || low.contains("redis") || low.contains("pandas") || low.contains("echarts")) {
                extra.add(token);
            } else {
                backend.add(token);
            }
        }
        need.setBackend(backend);
        need.setFrontend(frontend);
        need.setExtraStack(extra);
        return need;
    }

    public static boolean hasSignal(UserProfile profile) {
        if (profile == null) {
            return false;
        }
        return StringUtils.hasText(profile.getTechStack())
                || StringUtils.hasText(profile.getInterests())
                || StringUtils.hasText(profile.getBio());
    }

    private static List<String> split(String raw) {
        List<String> list = new ArrayList<String>();
        if (!StringUtils.hasText(raw)) {
            return list;
        }
        for (String part : raw.split("[,，/、|+]+")) {
            if (StringUtils.hasText(part)) {
                list.add(part.trim());
            }
        }
        return list;
    }
}
