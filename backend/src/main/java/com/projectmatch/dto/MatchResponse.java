package com.projectmatch.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchResponse {
    private UserNeedProfile profile;
    private String summary;
    private List<MatchResult> recommendations;
}
