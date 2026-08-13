package com.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialConsumption {
    private Integer id;
    private Integer orderId;
    private Integer materialId;
    private Integer quantity;
    private LocalDateTime createdAt;
}