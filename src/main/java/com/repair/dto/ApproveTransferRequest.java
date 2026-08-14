package com.repair.dto;

import lombok.Data;

/**
 * 客服同意转单/协助申请请求
 */
@Data
public class ApproveTransferRequest {
    private Integer newWorkerId;
}