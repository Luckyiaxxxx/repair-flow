package com.repair.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.repair.common.ValidationGroups;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {

    @NotNull(groups = {ValidationGroups.Update.class}, message = "用户ID不能为空")
    private Integer id;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "用户名不能为空")
    @Size(groups = {ValidationGroups.Add.class}, min = 3, max = 20, message = "用户名必须在3-20位之间")
    private String username;

    @NotBlank(groups = {ValidationGroups.Add.class}, message = "密码不能为空")
    @Size(groups = {ValidationGroups.Add.class}, min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度必须在2-20位之间")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(groups = {ValidationGroups.Add.class}, message = "角色不能为空")
    @Min(groups = {ValidationGroups.Add.class}, value = 1, message = "角色值必须在1-4之间")
    @Max(groups = {ValidationGroups.Add.class}, value = 4, message = "角色值必须在1-4之间")
    private Integer role;

    private Integer status;
    private String building;
    private String unit;
    private String room;
    private String skill;
    private Integer maxWorkload;
    private Integer onDuty;
    private String serviceArea;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}