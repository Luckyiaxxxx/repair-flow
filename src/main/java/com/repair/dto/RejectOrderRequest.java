package com.repair.dto;

import lombok.Data;

/**
 * 客服驳回工单请求
 */
@Data
public class RejectOrderRequest {
    private String reason;
}