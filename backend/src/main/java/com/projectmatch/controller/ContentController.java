package com.projectmatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.ApiResult;
import com.projectmatch.entity.Article;
import com.projectmatch.entity.SiteMessage;
import com.projectmatch.mapper.ArticleMapper;
import com.projectmatch.mapper.SiteMessageMapper;
import com.projectmatch.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    private final ArticleMapper articleMapper;
    private final SiteMessageMapper messageMapper;

    public ContentController(ArticleMapper articleMapper, SiteMessageMapper messageMapper) {
        this.articleMapper = articleMapper;
        this.messageMapper = messageMapper;
    }

    @GetMapping("/api/articles")
    public ApiResult<List<Article>> articles() {
        return ApiResult.ok(articleMapper.selectList(new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreateTime)));
    }

    @GetMapping("/api/articles/{id}")
    public ApiResult<Article> article(@PathVariable Long id) {
        return ApiResult.ok(articleMapper.selectById(id));
    }

    @GetMapping("/api/messages")
    public ApiResult<List<SiteMessage>> messages() {
        return ApiResult.ok(messageMapper.selectList(new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, SecurityUtils.currentUserId())
                .orderByDesc(SiteMessage::getCreateTime)));
    }

    @GetMapping("/api/messages/unread-count")
    public ApiResult<Long> unreadCount() {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, SecurityUtils.currentUserId())
                .eq(SiteMessage::getReadFlag, 0));
        return ApiResult.ok(count == null ? 0L : count);
    }

    @PostMapping("/api/messages/{id}/read")
    public ApiResult<String> read(@PathVariable Long id) {
        SiteMessage message = messageMapper.selectById(id);
        if (message != null && message.getUserId().equals(SecurityUtils.currentUserId())) {
            message.setReadFlag(1);
            messageMapper.updateById(message);
        }
        return ApiResult.ok("ok");
    }

    @GetMapping("/api/meta/filters")
    public ApiResult<Map<String, Object>> filters() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("categories", Arrays.asList("Java", "Python", "人工智能", "移动开发"));
        map.put("difficulties", Arrays.asList("easy", "medium", "hard"));
        map.put("projectTypes", Arrays.asList("毕业设计", "课程设计", "求职项目"));
        return ApiResult.ok(map);
    }
}
