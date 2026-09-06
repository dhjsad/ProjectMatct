package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.ai.MatchScorer;
import com.projectmatch.ai.NeedAnalyzer;
import com.projectmatch.dto.MatchResponse;
import com.projectmatch.dto.MatchResult;
import com.projectmatch.dto.UserNeedProfile;
import com.projectmatch.dto.UserNeedRequest;
import com.projectmatch.entity.Project;
import com.projectmatch.entity.UserRecommendation;
import com.projectmatch.mapper.ProjectMapper;
import com.projectmatch.mapper.UserRecommendationMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchService {

    private final NeedAnalyzer needAnalyzer;
    private final MatchScorer matchScorer;
    private final ProjectMapper projectMapper;
    private final UserRecommendationMapper recommendationMapper;

    public MatchService(NeedAnalyzer needAnalyzer, MatchScorer matchScorer,
                        ProjectMapper projectMapper, UserRecommendationMapper recommendationMapper) {
        this.needAnalyzer = needAnalyzer;
        this.matchScorer = matchScorer;
        this.projectMapper = projectMapper;
        this.recommendationMapper = recommendationMapper;
    }

    public UserNeedProfile analyze(UserNeedRequest request) {
        return needAnalyzer.analyze(request);
    }

    public MatchResponse match(UserNeedRequest request, Long userId) {
        UserNeedProfile profile = needAnalyzer.analyze(request);
        List<Project> projects = projectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, "PUBLISHED"));
        List<MatchResult> scored = new ArrayList<MatchResult>();
        for (Project project : projects) {
            scored.add(matchScorer.score(profile, project));
        }
        Collections.sort(scored, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return Integer.compare(b.getMatchScore(), a.getMatchScore());
            }
        });
        int limit = Math.min(5, scored.size());
        List<MatchResult> top = new ArrayList<MatchResult>(scored.subList(0, limit));
        if (userId != null) {
            for (MatchResult item : top) {
                UserRecommendation rec = new UserRecommendation();
                rec.setUserId(userId);
                rec.setProjectId(item.getProject().getId());
                rec.setMatchScore(BigDecimal.valueOf(item.getMatchScore()));
                rec.setRecommendReason(String.join("\n", item.getReasons()));
                rec.setStatus("SHOWN");
                rec.setCreateTime(LocalDateTime.now());
                recommendationMapper.insert(rec);
            }
        }
        MatchResponse response = new MatchResponse();
        response.setProfile(profile);
        response.setRecommendations(top);
        if (top.isEmpty()) {
            response.setSummary("暂时没有找到合适的项目，试试放宽技术栈或难度条件。");
        } else {
            MatchResult best = top.get(0);
            response.setSummary("根据你的技术水平，更推荐「" + best.getProject().getName()
                    + "」。匹配度 " + best.getMatchScore() + "%。平台提供的是学习与开发辅助，请在理解架构后自行完成实现。");
        }
        return response;
    }
}
