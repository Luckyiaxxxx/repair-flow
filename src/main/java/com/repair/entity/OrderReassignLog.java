package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转派记录
 */
@Data
public class OrderReassignLog {
    private Integer id;
    private Integer orderId;
    private Integer oldWorkerId;
    private Integer newWorkerId;
    private Integer dispatcherId;
    private Integer source; // 1-客服转派 2-转单同意 3-协助加派
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}