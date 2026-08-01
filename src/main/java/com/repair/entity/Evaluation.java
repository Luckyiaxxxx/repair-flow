package com.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Evaluation {
    private Integer id;
    private Integer orderId;
    private Integer ownerId;
    private Integer workerId;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}