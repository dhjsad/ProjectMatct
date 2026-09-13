package com.projectmatch.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private List<PurchaseRequest> items;
}
