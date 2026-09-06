package com.projectmatch.ai;

import com.projectmatch.dto.MatchResult;
import com.projectmatch.dto.UserNeedProfile;
import com.projectmatch.entity.Project;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class MatchScorer {

    public MatchResult score(UserNeedProfile need, Project project) {
        int tech = techScore(need, project);
        int difficulty = difficultyScore(need, project);
        int type = typeScore(need, project);
        int interest = interestScore(need, project);
        int duration = durationScore(need, project);
        int total = (int) Math.round(tech * 0.30 + difficulty * 0.25 + type * 0.20 + interest * 0.15 + duration * 0.10);

        MatchResult result = new MatchResult();
        result.setProject(project);
        result.setTechScore(tech);
        result.setDifficultyScore(difficulty);
        result.setTypeScore(type);
        result.setInterestScore(interest);
        result.setDurationScore(duration);
        result.setMatchScore(total);
        result.setReasons(reasons(need, project, tech, difficulty, type, interest, duration));
        return result;
    }

    private int techScore(UserNeedProfile need, Project project) {
        Set<String> user = new LinkedHashSet<String>();
        addAll(user, need.getLanguage());
        addAll(user, need.getBackend());
        addAll(user, need.getFrontend());
        addAll(user, need.getExtraStack());
        Set<String> proj = tokens(project.getTechStack());
        if (user.isEmpty() || proj.isEmpty()) {
            return 60;
        }
        int hit = 0;
        for (String item : proj) {
            if (containsSimilar(user, item)) {
                hit++;
            }
        }
        return clamp((int) Math.round(100.0 * hit / proj.size()));
    }

    private int difficultyScore(UserNeedProfile need, Project project) {
        int user = level(need.getDifficulty(), need.getSkillLevel());
        int proj = level(project.getDifficulty(), null);
        int gap = Math.abs(user - proj);
        if (gap == 0) return 100;
        if (gap == 1) return 75;
        return 40;
    }

    private int typeScore(UserNeedProfile need, Project project) {
        if (!StringUtils.hasText(need.getProjectType()) || !StringUtils.hasText(project.getProjectType())) {
            return 70;
        }
        if (need.getProjectType().equals(project.getProjectType())) {
            return 100;
        }
        if ("毕业设计".equals(need.getProjectType()) && "求职项目".equals(project.getProjectType())) {
            return 70;
        }
        return 45;
    }

    private int interestScore(UserNeedProfile need, Project project) {
        String blob = (safe(need.getInterests()) + " " + safe(need.getRawQuery())).toLowerCase(Locale.ROOT);
        String projectBlob = (safe(project.getName()) + " " + safe(project.getDescription()) + " " + safe(project.getModules())).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(need.getInterests()) && !StringUtils.hasText(need.getRawQuery())) {
            return 70;
        }
        String[] keys = {"宠物", "医疗", "医院", "养老", "校园", "图书", "电商", "二手", "考试", "选课", "动物", "数据", "机器学习", "小程序"};
        int hit = 0;
        int mention = 0;
        for (String key : keys) {
            if (blob.contains(key)) {
                mention++;
                if (projectBlob.contains(key)) {
                    hit++;
                }
            }
        }
        if (mention == 0) {
            return 65;
        }
        return clamp((int) Math.round(50 + 50.0 * hit / mention));
    }

    private int durationScore(UserNeedProfile need, Project project) {
        if (need.getDurationDays() == null || project.getEstimatedDuration() == null) {
            return 70;
        }
        int user = need.getDurationDays();
        int proj = project.getEstimatedDuration();
        int gap = Math.abs(user - proj);
        double ratio = 1.0 - Math.min(1.0, gap / (double) Math.max(user, 1));
        return clamp((int) Math.round(ratio * 100));
    }

    private List<String> reasons(UserNeedProfile need, Project project, int tech, int difficulty, int type, int interest, int duration) {
        List<String> list = new ArrayList<String>();
        if (tech >= 80) {
            list.add("技术栈高度重合：" + project.getTechStack() + "，你现有基础可以直接上手。");
        } else if (tech >= 50) {
            list.add("核心技术栈匹配，部分模块可以作为学习扩展。");
        } else {
            list.add("技术栈有差异，适合作为进阶挑战，而不是无痛复用现有 CRUD 经验。");
        }
        if (difficulty >= 80) {
            list.add("项目难度与你的水平接近，不涉及过重的架构负担。");
        } else if ("hard".equals(project.getDifficulty())) {
            list.add("项目偏难，包含更复杂的架构，建议预留更长开发周期。");
        } else {
            list.add("难度略有偏差，可通过裁剪模块或增加亮点来适配。");
        }
        list.add("项目类型偏向「" + project.getProjectType() + "」，预计开发周期约 " + project.getEstimatedDuration() + " 天。");
        if (interest >= 80) {
            list.add("业务方向和你提到的兴趣高度相关。");
        }
        if ("feature-rich".equals(need.getProjectRequirement())) {
            list.add("功能模块较完整，适合作为毕业设计展示，而不是只有单表增删改查。");
        }
        if (duration < 60 && need.getDurationDays() != null && project.getEstimatedDuration() > need.getDurationDays()) {
            list.add("当前时间偏紧，建议先完成核心模块，再视情况扩展。");
        }
        if ("微服务".equals(project.getCategory()) || (project.getTechStack() != null && project.getTechStack().contains("Spring Cloud"))) {
            list.add("包含微服务，初级开发者需要额外学习注册中心与服务拆分。");
        }
        return list;
    }

    private static void addAll(Set<String> set, String value) {
        if (StringUtils.hasText(value)) {
            set.add(value.toLowerCase(Locale.ROOT));
        }
    }

    private static void addAll(Set<String> set, List<String> values) {
        if (values == null) return;
        for (String value : values) {
            addAll(set, value);
        }
    }

    private static Set<String> tokens(String raw) {
        Set<String> set = new LinkedHashSet<String>();
        if (!StringUtils.hasText(raw)) return set;
        for (String part : raw.split("[,，/、|+]+")) {
            if (StringUtils.hasText(part)) {
                set.add(part.trim().toLowerCase(Locale.ROOT));
            }
        }
        return set;
    }

    private static boolean containsSimilar(Set<String> user, String item) {
        for (String u : user) {
            if (u.contains(item) || item.contains(u)) {
                return true;
            }
            if (u.contains("vue") && item.contains("vue")) return true;
            if (u.contains("spring") && item.contains("spring")) return true;
        }
        return false;
    }

    private static int level(String difficulty, String skill) {
        String value = StringUtils.hasText(difficulty) ? difficulty : skill;
        if ("easy".equals(value) || "junior".equals(value)) return 1;
        if ("hard".equals(value) || "senior".equals(value)) return 3;
        return 2;
    }

    private static int clamp(int n) {
        return Math.max(0, Math.min(100, n));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
