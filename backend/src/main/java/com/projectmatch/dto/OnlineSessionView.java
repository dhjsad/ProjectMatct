package com.projectmatch.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnlineSessionView {
    private String visitorId;
    private Long userId;
    private String username;
    private boolean guest;
    private String path;
    private String ip;
    private LocalDateTime lastSeen;
}
