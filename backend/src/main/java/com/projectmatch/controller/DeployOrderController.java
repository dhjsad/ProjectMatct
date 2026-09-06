package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.dto.DeployOrderRequest;
import com.projectmatch.entity.DeployOrder;
import com.projectmatch.security.SecurityUtils;
import com.projectmatch.service.DeployOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DeployOrderController {

    private final DeployOrderService deployOrderService;

    public DeployOrderController(DeployOrderService deployOrderService) {
        this.deployOrderService = deployOrderService;
    }

    @PostMapping("/projects/{id}/deploy-orders")
    public ApiResult<DeployOrder> create(@PathVariable Long id, @RequestBody(required = false) DeployOrderRequest request) {
        return ApiResult.ok(deployOrderService.create(id, SecurityUtils.currentUserId(), request == null ? new DeployOrderRequest() : request));
    }

    @PostMapping("/deploy-orders/{orderId}/pay")
    public ApiResult<DeployOrder> pay(@PathVariable Long orderId) {
        return ApiResult.ok(deployOrderService.pay(orderId, SecurityUtils.currentUserId()));
    }

    @GetMapping("/deploy-orders/mine")
    public ApiResult<List<DeployOrder>> mine() {
        return ApiResult.ok(deployOrderService.mine(SecurityUtils.currentUserId()));
    }
}
