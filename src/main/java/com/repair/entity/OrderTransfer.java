package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转单/协助申请
 */
@Data
public class OrderTransfer {
    private Integer id;
    private Integer orderId;
    private Integer fromWorkerId;
    private Integer type; // 1-转单 2-申请协助
    private String reason;
    private Integer status; // 0-待处理 1-已同意 2-已拒绝
    private Integer dispatcherId;
    private Integer toWorkerId;
    private String handleNote;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime handledAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}