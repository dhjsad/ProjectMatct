package com.projectmatch.dto;

import lombok.Data;

@Data
public class SiteBannerSaveRequest {
    private String slotKey;
    private String content;
    private String fontFamily;
    private String fontSize;
    private String color;
    private String bgColor;
    private Integer enabledFlag;
}
