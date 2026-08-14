package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMessage {

    /** 消息类型常量 */
    public static final int TYPE_ASSIGN = 1;      // 派单通知（维修工）
    public static final int TYPE_ACCEPT = 2;      // 接单通知（业主）
    public static final int TYPE_COMPLETE = 3;    // 完工通知（业主）
    public static final int TYPE_EVALUATE_REMIND = 4; // 评价提醒（业主）
    public static final int TYPE_SYSTEM = 5;      // 系统通知

    private Integer id;
    private Integer receiverId;
    private Integer type;
    private String title;
    private String content;
    private Integer orderId;
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime readAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}