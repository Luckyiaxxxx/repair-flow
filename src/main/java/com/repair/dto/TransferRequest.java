package com.repair.dto;

import lombok.Data;

/**
 * 维修工转单/协助申请请求
 */
@Data
public class TransferRequest {
    private Integer type; // 1-转单 2-申请协助
    private String reason;
}