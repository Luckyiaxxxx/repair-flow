package com.repair.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompleteOrderRequest {
    private String repairNote;
    private Integer repairDuration;
    private Double laborCost;
    private List<MaterialItem> materials;

    @Data
    public static class MaterialItem {
        private Integer materialId;
        private Integer quantity;
    }
}