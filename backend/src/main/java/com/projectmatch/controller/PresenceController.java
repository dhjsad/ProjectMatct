package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.dto.HeartbeatRequest;
import com.projectmatch.service.PresenceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping("/heartbeat")
    public ApiResult<Map<String, Object>> heartbeat(@Valid @RequestBody HeartbeatRequest request,
                                                    HttpServletRequest http) {
        return ApiResult.ok(presenceService.heartbeat(request, http));
    }
}
