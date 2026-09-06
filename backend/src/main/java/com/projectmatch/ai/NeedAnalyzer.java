package com.projectmatch.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmatch.dto.UserNeedProfile;
import com.projectmatch.dto.UserNeedRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NeedAnalyzer {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final boolean aiEnabled;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public NeedAnalyzer(ObjectMapper objectMapper,
                        @Value("${ai.enabled}") boolean aiEnabled,
                        @Value("${ai.base-url}") String baseUrl,
                        @Value("${ai.api-key}") String apiKey,
                        @Value("${ai.model}") String model) {
        this.objectMapper = objectMapper;
        this.aiEnabled = aiEnabled;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public UserNeedProfile analyze(UserNeedRequest request) {
        UserNeedProfile fromForm = fromForm(request);
        if (StringUtils.hasText(request.getQuery())) {
            UserNeedProfile parsed = tryLlm(request.getQuery());
            if (parsed == null) {
                parsed = ruleBased(request.getQuery());
            }
            return merge(parsed, fromForm);
        }
        fromForm.setRawQuery("");
        return fromForm;
    }

    private UserNeedProfile fromForm(UserNeedRequest request) {
        UserNeedProfile profile = new UserNeedProfile();
        profile.setLanguage(request.getLanguage());
        profile.setSkillLevel(request.getSkillLevel());
        profile.setDifficulty(request.getDifficulty());
        profile.setProjectType(request.getProjectType());
        profile.setInterests(request.getInterests());
        profile.setDurationDays(request.getDurationDays());
        profile.setProjectRequirement(request.getProjectRequirement());
        if (StringUtils.hasText(request.getBackend())) {
            profile.setBackend(split(request.getBackend()));
        }
        if (StringUtils.hasText(request.getFrontend())) {
            profile.setFrontend(split(request.getFrontend()));
        }
        return profile;
    }

    private UserNeedProfile merge(UserNeedProfile parsed, UserNeedProfile form) {
        if (StringUtils.hasText(form.getLanguage())) parsed.setLanguage(form.getLanguage());
        if (StringUtils.hasText(form.getSkillLevel())) parsed.setSkillLevel(form.getSkillLevel());
        if (StringUtils.hasText(form.getDifficulty())) parsed.setDifficulty(form.getDifficulty());
        if (StringUtils.hasText(form.getProjectType())) parsed.setProjectType(form.getProjectType());
        if (StringUtils.hasText(form.getInterests())) parsed.setInterests(form.getInterests());
        if (form.getDurationDays() != null) parsed.setDurationDays(form.getDurationDays());
        if (StringUtils.hasText(form.getProjectRequirement())) parsed.setProjectRequirement(form.getProjectRequirement());
        if (form.getBackend() != null && !form.getBackend().isEmpty()) parsed.setBackend(form.getBackend());
        if (form.getFrontend() != null && !form.getFrontend().isEmpty()) parsed.setFrontend(form.getFrontend());
        return parsed;
    }

    private UserNeedProfile tryLlm(String query) {
        if (!aiEnabled || !StringUtils.hasText(apiKey)) {
            return null;
        }
        try {
            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("model", model);
            List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
            messages.add(msg("system", "你是毕设工坊的需求分析助手。从学生描述中提取结构化 JSON，只输出 JSON，不要 markdown。"
                    + "字段：language, skillLevel(junior|intermediate|senior), backend(array), frontend(array),"
                    + " difficulty(easy|medium|hard), projectType, interests, durationDays(number|null),"
                    + " projectRequirement(feature-rich|simple|resume)."));
            messages.add(msg("user", query));
            payload.put("messages", messages);
            payload.put("temperature", 0.2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    trimSlash(baseUrl) + "/chat/completions",
                    new HttpEntity<Map<String, Object>>(payload, headers),
                    String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            content = content.replaceAll("(?s)```json", "").replace("```", "").trim();
            JsonNode json = objectMapper.readTree(content);
            UserNeedProfile profile = new UserNeedProfile();
            profile.setLanguage(text(json, "language"));
            profile.setSkillLevel(text(json, "skillLevel"));
            profile.setDifficulty(text(json, "difficulty"));
            profile.setProjectType(text(json, "projectType"));
            profile.setInterests(text(json, "interests"));
            profile.setProjectRequirement(text(json, "projectRequirement"));
            if (json.has("durationDays") && !json.get("durationDays").isNull()) {
                profile.setDurationDays(json.get("durationDays").asInt());
            }
            profile.setBackend(toList(json.get("backend")));
            profile.setFrontend(toList(json.get("frontend")));
            profile.setRawQuery(query);
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    public UserNeedProfile ruleBased(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        UserNeedProfile profile = new UserNeedProfile();
        profile.setRawQuery(query);

        if (containsAny(q, "python", "django", "flask", "pytorch")) {
            profile.setLanguage("Python");
        } else if (containsAny(q, "android", "kotlin")) {
            profile.setLanguage("Android");
        } else if (containsAny(q, "小程序", "微信")) {
            profile.setLanguage("微信小程序");
        } else {
            profile.setLanguage("Java");
        }

        List<String> backend = new ArrayList<String>();
        if (containsAny(q, "spring boot", "springboot", "spring")) backend.add("Spring Boot");
        if (containsAny(q, "mybatis")) backend.add("MyBatis");
        if (containsAny(q, "ssm")) backend.add("SSM");
        if (containsAny(q, "微服务", "spring cloud")) backend.add("Spring Cloud");
        if (containsAny(q, "django")) backend.add("Django");
        if (containsAny(q, "flask")) backend.add("Flask");
        if (backend.isEmpty() && "Java".equals(profile.getLanguage())) {
            backend.add("Spring Boot");
        }
        profile.setBackend(backend);

        List<String> frontend = new ArrayList<String>();
        if (containsAny(q, "vue")) frontend.add("Vue");
        if (containsAny(q, "react")) frontend.add("React");
        if (containsAny(q, "小程序")) frontend.add("微信小程序");
        profile.setFrontend(frontend);

        if (containsAny(q, "一般", "初学", "crud", "不会", "基础")) {
            profile.setSkillLevel("junior");
        } else if (containsAny(q, "一年", "熟练", "进阶")) {
            profile.setSkillLevel("intermediate");
        } else {
            profile.setSkillLevel("junior");
        }

        if (containsAny(q, "不要太难", "简单", "入门", "两周")) {
            profile.setDifficulty("easy");
        } else if (containsAny(q, "很难", "亮点", "微服务", "高并发")) {
            profile.setDifficulty("hard");
        } else {
            profile.setDifficulty("medium");
        }

        if (containsAny(q, "课程设计", "两周", "课程")) {
            profile.setProjectType("课程设计");
        } else if (containsAny(q, "简历", "求职", "作品")) {
            profile.setProjectType("求职项目");
        } else {
            profile.setProjectType("毕业设计");
        }

        List<String> interests = new ArrayList<String>();
        String[] tags = {"宠物", "医疗", "医院", "养老", "校园", "图书", "电商", "二手", "考试", "选课", "流浪动物", "数据分析", "机器学习"};
        for (String tag : tags) {
            if (query.contains(tag)) {
                interests.add(tag);
            }
        }
        profile.setInterests(interests.isEmpty() ? "综合业务系统" : String.join("、", interests));

        if (containsAny(q, "功能比较丰富", "功能丰富", "模块多")) {
            profile.setProjectRequirement("feature-rich");
        } else if (containsAny(q, "简历")) {
            profile.setProjectRequirement("resume");
        } else {
            profile.setProjectRequirement("simple");
        }

        Matcher matcher = Pattern.compile("(\\d+)\\s*(天|周|个月|月)").matcher(query);
        if (matcher.find()) {
            int n = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            if ("周".equals(unit)) {
                profile.setDurationDays(n * 7);
            } else if (unit.contains("月")) {
                profile.setDurationDays(n * 30);
            } else {
                profile.setDurationDays(n);
            }
        } else if (query.contains("两个月")) {
            profile.setDurationDays(60);
        } else if (profile.getProjectType().equals("课程设计")) {
            profile.setDurationDays(14);
        } else {
            profile.setDurationDays(40);
        }
        return profile;
    }

    private static Map<String, String> msg(String role, String content) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    private static List<String> split(String raw) {
        return new ArrayList<String>(Arrays.asList(raw.split("[,，/、\\s]+")));
    }

    private static List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<String>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private static String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static boolean containsAny(String q, String... keys) {
        for (String key : keys) {
            if (q.contains(key.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
