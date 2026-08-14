package com.repair.dto;

import lombok.Data;

/**
 * 工单留言请求
 */
@Data
public class MessageRequest {
    private String content;
}