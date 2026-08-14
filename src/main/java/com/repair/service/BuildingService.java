package com.repair.service;

import com.repair.entity.Building;
import com.repair.entity.BuildingUnit;

import java.util.List;
import java.util.Map;

public interface BuildingService {

    // ==================== 楼栋 ====================

    Building addBuilding(Building building);

    void deleteBuilding(Integer id);

    Building updateBuilding(Building building);

    Building getBuildingById(Integer id);

    List<Building> listAllBuildings();

    // ==================== 单元 ====================

    BuildingUnit addUnit(BuildingUnit unit);

    void deleteUnit(Integer id);

    BuildingUnit updateUnit(BuildingUnit unit);

    BuildingUnit getUnitById(Integer id);

    List<BuildingUnit> listUnits(Integer buildingId);

    // ==================== 业主端选择数据 ====================

    List<Map<String, Object>> listEnabledBuildingsWithUnits();
}