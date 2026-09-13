package com.projectmatch.controller;

import com.projectmatch.common.ApiResult;
import com.projectmatch.entity.SiteBanner;
import com.projectmatch.service.SiteBannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final SiteBannerService siteBannerService;

    public SiteController(SiteBannerService siteBannerService) {
        this.siteBannerService = siteBannerService;
    }

    @GetMapping("/banners")
    public ApiResult<List<SiteBanner>> banners() {
        return ApiResult.ok(siteBannerService.publicBanners());
    }
}
