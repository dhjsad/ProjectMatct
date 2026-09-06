package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.dto.HeartbeatRequest;
import com.projectmatch.dto.OnlineSessionView;
import com.projectmatch.entity.SiteVisit;
import com.projectmatch.mapper.SiteVisitMapper;
import com.projectmatch.security.AuthUser;
import com.projectmatch.security.SecurityUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private static final long ONLINE_TTL_MS = 120_000L;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final ConcurrentHashMap<String, OnlineSession> sessions = new ConcurrentHashMap<String, OnlineSession>();
    private final SiteVisitMapper visitMapper;

    public PresenceService(SiteVisitMapper visitMapper) {
        this.visitMapper = visitMapper;
    }

    public Map<String, Object> heartbeat(HeartbeatRequest request, HttpServletRequest http) {
        String visitorId = normalizeVisitorId(request.getVisitorId());
        String path = truncate(request.getPath(), 256);
        AuthUser user = SecurityUtils.currentUser();
        Long userId = user == null ? null : user.getId();
        String username = user == null ? null : user.getUsername();
        String ip = clientIp(http);
        String userAgent = truncate(http.getHeader("User-Agent"), 256);
        LocalDateTime now = LocalDateTime.now(ZONE);

        OnlineSession session = sessions.get(visitorId);
        if (session == null) {
            session = new OnlineSession();
            session.visitorId = visitorId;
            sessions.put(visitorId, session);
        }
        session.userId = userId;
        session.username = username;
        session.path = path;
        session.ip = ip;
        session.lastSeen = now;

        persistVisit(visitorId, userId, username, ip, path, userAgent, now);
        evictExpired(now);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("onlineCount", countOnline(now));
        return map;
    }

    public Map<String, Object> stats() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDate today = now.toLocalDate();
        evictExpired(now);

        List<OnlineSessionView> online = new ArrayList<OnlineSessionView>();
        Set<Long> onlineUserIds = new HashSet<Long>();
        int guestCount = 0;
        for (OnlineSession session : sessions.values()) {
            if (!session.alive(now)) {
                continue;
            }
            OnlineSessionView view = new OnlineSessionView();
            view.setVisitorId(session.visitorId);
            view.setUserId(session.userId);
            view.setUsername(session.username);
            view.setGuest(session.userId == null);
            view.setPath(session.path);
            view.setIp(session.ip);
            view.setLastSeen(session.lastSeen);
            online.add(view);
            if (session.userId == null) {
                guestCount++;
            } else {
                onlineUserIds.add(session.userId);
            }
        }
        int userCount = onlineUserIds.size();
        Collections.sort(online, new Comparator<OnlineSessionView>() {
            @Override
            public int compare(OnlineSessionView a, OnlineSessionView b) {
                if (a.getLastSeen() == null) {
                    return 1;
                }
                if (b.getLastSeen() == null) {
                    return -1;
                }
                return b.getLastSeen().compareTo(a.getLastSeen());
            }
        });

        List<SiteVisit> todayVisits = visitMapper.selectList(new LambdaQueryWrapper<SiteVisit>()
                .eq(SiteVisit::getVisitDate, today)
                .orderByDesc(SiteVisit::getLastSeen)
                .last("LIMIT 100"));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("onlineCount", online.size());
        map.put("onlineUserCount", userCount);
        map.put("onlineGuestCount", guestCount);
        map.put("todayVisitors", visitMapper.countDistinctVisitorsOn(today));
        map.put("totalVisitors", visitMapper.countDistinctVisitors());
        map.put("todayPageViews", visitMapper.sumPageViewsOn(today));
        map.put("totalPageViews", visitMapper.sumPageViews());
        map.put("onlineSessions", online);
        map.put("todayVisits", todayVisits);
        return map;
    }

    private void persistVisit(String visitorId, Long userId, String username, String ip,
                              String path, String userAgent, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        SiteVisit existing = visitMapper.selectOne(new LambdaQueryWrapper<SiteVisit>()
                .eq(SiteVisit::getVisitorId, visitorId)
                .eq(SiteVisit::getVisitDate, today));
        if (existing == null) {
            SiteVisit created = new SiteVisit();
            created.setVisitorId(visitorId);
            created.setUserId(userId);
            created.setUsername(username);
            created.setIp(ip);
            created.setPath(path);
            created.setUserAgent(userAgent);
            created.setVisitDate(today);
            created.setPageViews(1);
            created.setFirstSeen(now);
            created.setLastSeen(now);
            try {
                visitMapper.insert(created);
                return;
            } catch (DuplicateKeyException ignored) {
                existing = visitMapper.selectOne(new LambdaQueryWrapper<SiteVisit>()
                        .eq(SiteVisit::getVisitorId, visitorId)
                        .eq(SiteVisit::getVisitDate, today));
                if (existing == null) {
                    return;
                }
            }
        }
        boolean pathChanged = path != null && !path.equals(existing.getPath());
        existing.setUserId(userId != null ? userId : existing.getUserId());
        existing.setUsername(username != null ? username : existing.getUsername());
        existing.setIp(ip);
        existing.setPath(path);
        existing.setUserAgent(userAgent);
        existing.setLastSeen(now);
        if (pathChanged) {
            Integer views = existing.getPageViews();
            existing.setPageViews(views == null ? 1 : views + 1);
        }
        visitMapper.updateById(existing);
    }

    private void evictExpired(LocalDateTime now) {
        for (Map.Entry<String, OnlineSession> entry : sessions.entrySet()) {
            if (!entry.getValue().alive(now)) {
                sessions.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private int countOnline(LocalDateTime now) {
        int count = 0;
        for (OnlineSession session : sessions.values()) {
            if (session.alive(now)) {
                count++;
            }
        }
        return count;
    }

    private String normalizeVisitorId(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.length() > 64) {
            value = value.substring(0, 64);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_') {
                builder.append(ch);
            }
        }
        if (builder.length() == 0) {
            return "anon";
        }
        return builder.toString();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return truncate(forwarded.split(",")[0], 64);
        }
        return truncate(request.getRemoteAddr(), 64);
    }

    private static class OnlineSession {
        private String visitorId;
        private Long userId;
        private String username;
        private String path;
        private String ip;
        private LocalDateTime lastSeen;

        private boolean alive(LocalDateTime now) {
            return lastSeen != null && lastSeen.plusSeconds(ONLINE_TTL_MS / 1000).isAfter(now);
        }
    }
}
