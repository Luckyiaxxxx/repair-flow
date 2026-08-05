package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.repair.common.ValidationGroups;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairOrder {

    private Integer id;
    private String orderNo;

//    @NotNull(groups = {ValidationGroups.Add.class}, message = "业主ID不能为空")
    private Integer ownerId;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "楼栋不能为空")
    private String building;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "单元不能为空")
    private String unit;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "房号不能为空")
    private String room;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "报修类别不能为空")
    private String category;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "报修描述不能为空")
    @Size(groups = {ValidationGroups.Add.class}, min = 5, max = 500, message = "报修描述必须在5-500字之间")
    private String description;

    private String images;

    @Min(groups = {ValidationGroups.Add.class}, value = 1, message = "紧急程度必须在1-3之间")
    @Max(groups = {ValidationGroups.Add.class}, value = 3, message = "紧急程度必须在1-3之间")
    private Integer emergencyLevel;

    private Integer status;
    private Integer dispatcherId;
    private Integer workerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime assignedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime acceptedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime completedAt;

    private Integer repairDuration;
    private String repairNote;
    private Integer needRevisit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime timeoutAt;

    private Integer isTimeout;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}