package com.repair.dto;

import lombok.Data;

/**
 * 客服拒绝转单/协助申请请求
 */
@Data
public class RejectTransferRequest {
    private String note;
}