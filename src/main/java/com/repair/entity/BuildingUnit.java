package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.repair.common.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BuildingUnit {

    @NotNull(groups = {ValidationGroups.Update.class}, message = "单元ID不能为空")
    private Integer id;

    @NotNull(groups = {ValidationGroups.Add.class}, message = "所属楼栋ID不能为空")
    private Integer buildingId;

    @NotBlank(groups = {ValidationGroups.Add.class, ValidationGroups.Update.class}, message = "单元名称不能为空")
    @Size(groups = {ValidationGroups.Add.class, ValidationGroups.Update.class}, min = 1, max = 50, message = "单元名称长度必须在1-50位之间")
    private String name;

    private Integer sortOrder;

    private Integer status;

    // 列表展示用（JOIN 查询带回，非表字段）
    private String buildingName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}