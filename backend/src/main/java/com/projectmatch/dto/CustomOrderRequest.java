package com.projectmatch.dto;

import lombok.Data;

@Data
public class CustomOrderRequest {
    private String contactName;
    private String contact;
    private String company;
    private String title;
    private String requirement;
    private String budget;
}
