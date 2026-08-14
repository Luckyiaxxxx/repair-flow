package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.repair.common.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairCategory {

    @NotNull(groups = {ValidationGroups.Update.class}, message = "类别ID不能为空")
    private Integer id;

    @NotBlank(groups = {ValidationGroups.Add.class, ValidationGroups.Update.class}, message = "类别名称不能为空")
    @Size(groups = {ValidationGroups.Add.class, ValidationGroups.Update.class}, min = 2, max = 20, message = "类别名称长度必须在2-20位之间")
    private String name;

    private Integer sortOrder;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}