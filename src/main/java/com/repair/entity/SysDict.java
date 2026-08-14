package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.repair.common.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysDict {

    @NotNull(groups = {ValidationGroups.Update.class}, message = "字典ID不能为空")
    private Integer id;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "字典类型不能为空")
    @Size(groups = {ValidationGroups.Add.class}, min = 2, max = 50, message = "字典类型长度必须在2-50位之间")
    private String dictType;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "显示名称不能为空")
    @Size(groups = {ValidationGroups.Add.class}, min = 1, max = 50, message = "显示名称长度必须在1-50位之间")
    private String dictLabel;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "字典值不能为空")
    @Size(groups = {ValidationGroups.Add.class}, min = 1, max = 50, message = "字典值长度必须在1-50位之间")
    private String dictValue;

    private Integer sortOrder;

    private Integer status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}