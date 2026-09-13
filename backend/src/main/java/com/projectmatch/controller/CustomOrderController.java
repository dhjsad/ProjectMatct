package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.dto.CustomOrderRequest;
import com.projectmatch.entity.CustomOrder;
import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.CustomOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/custom-orders")
public class CustomOrderController {

    private final CustomOrderService customOrderService;

    public CustomOrderController(CustomOrderService customOrderService) {
        this.customOrderService = customOrderService;
    }

    @PostMapping
    public ApiResult<CustomOrder> create(@RequestBody CustomOrderRequest request) {
        return ApiResult.ok(customOrderService.create(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/mine")
    public ApiResult<List<CustomOrder>> mine() {
        return ApiResult.ok(customOrderService.mine(SecurityUtils.currentUserId()));
    }
}
