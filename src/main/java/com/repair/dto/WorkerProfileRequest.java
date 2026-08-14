package com.repair.dto;

import lombok.Data;

@Data
public class WorkerProfileRequest {

    private String skill;

    private Integer onDuty;

    private String serviceArea;

    private Integer maxWorkload;
}