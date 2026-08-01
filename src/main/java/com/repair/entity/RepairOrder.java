package com.repair.entity;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairOrder {
    private Integer id;
    private String orderNo;
    private Integer ownerId;
    private String building;
    private String unit;
    private String room;
    private String category;
    private String description;
    private String images;
    private Integer emergencyLevel;
    private Integer status;
    private Integer dispatcherId;
    private Integer workerId;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private Integer repairDuration;
    private String repairNote;
    private Integer needRevisit;
    private LocalDateTime timeoutAt;
    private Integer isTimeout;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
