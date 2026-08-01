package com.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;


@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Integer role;
    private Integer status;
    private String building;
    private String unit;
    private String room;
    private String skill;
    private Integer maxWorkload;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
