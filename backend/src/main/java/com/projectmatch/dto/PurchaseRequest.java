package com.projectmatch.dto;

import lombok.Data;

@Data
public class PurchaseRequest {
    private Long projectId;
    /** SELF_DOWNLOAD 自己下载源码；SERVICE 客服发送并含技术指导 */
    private String deliveryType;
    private String contact;
}
