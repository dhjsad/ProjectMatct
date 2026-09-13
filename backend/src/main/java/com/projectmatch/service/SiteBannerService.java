package com.projectmatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.projectmatch.common.BizException;
import com.projectmatch.dto.SiteBannerSaveRequest;
import com.projectmatch.entity.SiteBanner;
import com.projectmatch.mapper.SiteBannerMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class SiteBannerService {

    public static final String SLOT_TOP = "TOP";
    public static final String SLOT_SIDE = "SIDE";

    private final SiteBannerMapper bannerMapper;

    public SiteBannerService(SiteBannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    public void ensureDefaults() {
        ensure(SLOT_TOP, "\u8be5\u4f5c\u54c1\u53ea\u552e\u5356\u4e00\u6b21\uff0c\u4fdd\u8bc1\u72ec\u4e00\u65e0\u4e8c",
                "Inter, -apple-system, BlinkMacSystemFont, \"PingFang SC\", sans-serif",
                "13px", "#f5f5f7", "#1d1d1f");
        ensure(SLOT_SIDE, "\u72ec\u4e00\u65e0\u4e8c \u00b7 \u552e\u51fa\u5373\u4e0b\u67b6",
                "Inter, -apple-system, BlinkMacSystemFont, \"PingFang SC\", sans-serif",
                "13px", "#1d1d1f", "#f5f5f7");
    }

    private void ensure(String slot, String content, String font, String size, String color, String bg) {
        Long count = bannerMapper.selectCount(new LambdaQueryWrapper<SiteBanner>().eq(SiteBanner::getSlotKey, slot));
        if (count != null && count > 0) {
            return;
        }
        SiteBanner banner = new SiteBanner();
        banner.setSlotKey(slot);
        banner.setContent(content);
        banner.setFontFamily(font);
        banner.setFontSize(size);
        banner.setColor(color);
        banner.setBgColor(bg);
        banner.setEnabledFlag(1);
        banner.setUpdateTime(LocalDateTime.now());
        bannerMapper.insert(banner);
    }

    public List<SiteBanner> publicBanners() {
        ensureDefaults();
        return bannerMapper.selectList(new LambdaQueryWrapper<SiteBanner>()
                .eq(SiteBanner::getEnabledFlag, 1)
                .in(SiteBanner::getSlotKey, Arrays.asList(SLOT_TOP, SLOT_SIDE)));
    }

    public List<SiteBanner> all() {
        ensureDefaults();
        return bannerMapper.selectList(new LambdaQueryWrapper<SiteBanner>().orderByAsc(SiteBanner::getId));
    }

    public SiteBanner save(SiteBannerSaveRequest request) {
        if (request == null || !StringUtils.hasText(request.getSlotKey())) {
            throw new BizException("缺少栏位");
        }
        String slot = request.getSlotKey().trim().toUpperCase();
        if (!SLOT_TOP.equals(slot) && !SLOT_SIDE.equals(slot)) {
            throw new BizException("仅支持顶部公告或侧边广告栏");
        }
        SiteBanner banner = bannerMapper.selectOne(new LambdaQueryWrapper<SiteBanner>().eq(SiteBanner::getSlotKey, slot));
        if (banner == null) {
            banner = new SiteBanner();
            banner.setSlotKey(slot);
        }
        banner.setContent(request.getContent());
        banner.setFontFamily(StringUtils.hasText(request.getFontFamily()) ? request.getFontFamily() : banner.getFontFamily());
        banner.setFontSize(StringUtils.hasText(request.getFontSize()) ? request.getFontSize() : banner.getFontSize());
        banner.setColor(StringUtils.hasText(request.getColor()) ? request.getColor() : banner.getColor());
        banner.setBgColor(StringUtils.hasText(request.getBgColor()) ? request.getBgColor() : banner.getBgColor());
        banner.setEnabledFlag(request.getEnabledFlag() == null ? 1 : request.getEnabledFlag());
        banner.setUpdateTime(LocalDateTime.now());
        if (banner.getId() == null) {
            bannerMapper.insert(banner);
        } else {
            bannerMapper.updateById(banner);
        }
        return banner;
    }
}
