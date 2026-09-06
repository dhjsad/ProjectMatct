package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.ai.MatchScorer;
import com.projectmatch.ai.ProfileNeedMapper;
import com.projectmatch.dto.MatchResult;
import com.projectmatch.dto.UserNeedProfile;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.SiteMessage;
import com.projectmatch.entity.SysUser;
import com.projectmatch.entity.UserProfile;
import com.projectmatch.entity.UserRecommendation;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.SiteMessageMapper;
import com.projectmatch.mapper.SysUserMapper;
import com.projectmatch.mapper.UserProfileMapper;
import com.projectmatch.mapper.UserRecommendationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ProactiveMatchService {

    private static final Logger log = LoggerFactory.getLogger(ProactiveMatchService.class);

    private final ProjectMapper projectMapper;
    private final SysUserMapper userMapper;
    private final UserProfileMapper profileMapper;
    private final UserRecommendationMapper recommendationMapper;
    private final SiteMessageMapper messageMapper;
    private final MatchScorer matchScorer;
    private final int minScore;
    private final int topN;

    public ProactiveMatchService(ProjectMapper projectMapper,
                                 SysUserMapper userMapper,
                                 UserProfileMapper profileMapper,
                                 UserRecommendationMapper recommendationMapper,
                                 SiteMessageMapper messageMapper,
                                 MatchScorer matchScorer,
                                 @Value("${recommend.min-score:68}") int minScore,
                                 @Value("${recommend.top-n:8}") int topN) {
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.recommendationMapper = recommendationMapper;
        this.messageMapper = messageMapper;
        this.matchScorer = matchScorer;
        this.minScore = minScore;
        this.topN = topN;
    }

    @Transactional
    public int matchAndNotify(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !"PUBLISHED".equals(project.getStatus())) {
            return 0;
        }
        List<Candidate> scored = new ArrayList<Candidate>();
        List<UserProfile> profiles = profileMapper.selectList(new LambdaQueryWrapper<UserProfile>());
        for (UserProfile profile : profiles) {
            if (!ProfileNeedMapper.hasSignal(profile)) {
                continue;
            }
            SysUser user = userMapper.selectById(profile.getUserId());
            if (user == null || "ADMIN".equals(user.getRole())) {
                continue;
            }
            if (alreadyPushed(profile.getUserId(), projectId)) {
                continue;
            }
            UserNeedProfile need = ProfileNeedMapper.from(profile);
            MatchResult result = matchScorer.score(need, project);
            scored.add(new Candidate(profile.getUserId(), result));
        }
        Collections.sort(scored, new Comparator<Candidate>() {
            @Override
            public int compare(Candidate a, Candidate b) {
                return Integer.compare(b.result.getMatchScore(), a.result.getMatchScore());
            }
        });
        int limit = Math.min(topN, scored.size());
        int notified = 0;
        for (int i = 0; i < limit; i++) {
            Candidate item = scored.get(i);
            if (item.result.getMatchScore() < minScore) {
                continue;
            }
            persist(item.userId, project, item.result);
            notified++;
        }
        log.info("project {} proactive match notified {}", projectId, notified);
        return notified;
    }

    private boolean alreadyPushed(Long userId, Long projectId) {
        Long count = recommendationMapper.selectCount(new LambdaQueryWrapper<UserRecommendation>()
                .eq(UserRecommendation::getUserId, userId)
                .eq(UserRecommendation::getProjectId, projectId)
                .eq(UserRecommendation::getStatus, "PUSHED"));
        return count != null && count > 0;
    }

    private void persist(Long userId, Project project, MatchResult result) {
        UserRecommendation rec = new UserRecommendation();
        rec.setUserId(userId);
        rec.setProjectId(project.getId());
        rec.setMatchScore(BigDecimal.valueOf(result.getMatchScore()));
        rec.setRecommendReason(String.join("\n", result.getReasons()));
        rec.setStatus("PUSHED");
        rec.setCreateTime(LocalDateTime.now());
        recommendationMapper.insert(rec);

        StringBuilder content = new StringBuilder();
        content.append(project.getName()).append("\n");
        content.append("匹配度：").append(result.getMatchScore()).append("%\n");
        if (result.getReasons() != null) {
            for (String reason : result.getReasons()) {
                content.append("✓ ").append(reason).append("\n");
            }
        }
        content.append("这是按你的技术画像主动推送的学习推荐，请理解架构后自行实现，不要直接作为作业提交。");

        SiteMessage message = new SiteMessage();
        message.setUserId(userId);
        message.setProjectId(project.getId());
        message.setMsgType("PROJECT_PUSH");
        message.setTitle("发现一个可能适合你的项目");
        message.setContent(content.toString());
        message.setReadFlag(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private static class Candidate {
        private final Long userId;
        private final MatchResult result;

        private Candidate(Long userId, MatchResult result) {
            this.userId = userId;
            this.result = result;
        }
    }
}
