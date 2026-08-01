package com.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Material {
    private Integer id;
    private String name;
    private String category;
    private String spec;
    private String unit;
    private Integer stock;
    private Integer minStock;
    private Double price;
    private String supplier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}