package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单留言
 */
@Data
public class OrderMessage {
    private Integer id;
    private Integer orderId;
    private Integer senderId;
    private Integer senderRole; // 1-业主 2-客服 3-维修工
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}