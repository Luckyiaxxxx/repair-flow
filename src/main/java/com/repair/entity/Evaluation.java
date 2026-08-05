package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.repair.common.ValidationGroups;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Evaluation {
    private Integer id;

    @NotNull(groups = {ValidationGroups.Add.class},message = "报修单ID不能为空")
    private Integer orderId;

    @NotNull(groups = {ValidationGroups.Add.class},message = "业主ID不能为空")
    private Integer ownerId;

    private Integer workerId;

    @NotNull(groups = {ValidationGroups.Add.class},message = "评分不能为空")
    @Min(groups = {ValidationGroups.Add.class},value = 1,message = "评分必须在1~5星之间")
    @Max(groups = {ValidationGroups.Add.class},value = 5,message = "评分必须在1~5星之间")
    private Integer score;

    @Size(groups = {ValidationGroups.Add.class},max = 255,message = "评价内容不能超过225字")
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
}