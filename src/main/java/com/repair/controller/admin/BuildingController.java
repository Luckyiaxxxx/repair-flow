package com.repair.controller.admin;

//管理端：楼栋/单元基础数据管理

import com.repair.common.Result;
import com.repair.common.ValidationGroups;
import com.repair.entity.Building;
import com.repair.entity.BuildingUnit;
import com.repair.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    // ==================== 楼栋 ====================

    @PostMapping("/buildings")
    public Result<Building> addBuilding(@Validated(ValidationGroups.Add.class) @RequestBody Building building) {
        Building saved = buildingService.addBuilding(building);
        return Result.success(saved);
    }

    @DeleteMapping("/buildings/{id}")
    public Result<String> deleteBuilding(@PathVariable Integer id) {
        buildingService.deleteBuilding(id);
        return Result.success("删除成功");
    }

    @PutMapping("/buildings")
    public Result<Building> updateBuilding(@Validated(ValidationGroups.Update.class) @RequestBody Building building) {
        Building updated = buildingService.updateBuilding(building);
        return Result.success(updated);
    }

    @GetMapping("/buildings/{id}")
    public Result<Building> getBuildingById(@PathVariable Integer id) {
        Building building = buildingService.getBuildingById(id);
        return Result.success(building);
    }

    @GetMapping("/buildings")
    public Result<List<Building>> listAllBuildings() {
        List<Building> buildings = buildingService.listAllBuildings();
        return Result.success(buildings);
    }

    // ==================== 单元 ====================

    @PostMapping("/units")
    public Result<BuildingUnit> addUnit(@Validated(ValidationGroups.Add.class) @RequestBody BuildingUnit unit) {
        BuildingUnit saved = buildingService.addUnit(unit);
        return Result.success(saved);
    }

    @DeleteMapping("/units/{id}")
    public Result<String> deleteUnit(@PathVariable Integer id) {
        buildingService.deleteUnit(id);
        return Result.success("删除成功");
    }

    @PutMapping("/units")
    public Result<BuildingUnit> updateUnit(@Validated(ValidationGroups.Update.class) @RequestBody BuildingUnit unit) {
        BuildingUnit updated = buildingService.updateUnit(unit);
        return Result.success(updated);
    }

    @GetMapping("/units/{id}")
    public Result<BuildingUnit> getUnitById(@PathVariable Integer id) {
        BuildingUnit unit = buildingService.getUnitById(id);
        return Result.success(unit);
    }

    @GetMapping("/units")
    public Result<List<BuildingUnit>> listUnits(@RequestParam(required = false) Integer buildingId) {
        List<BuildingUnit> units = buildingService.listUnits(buildingId);
        return Result.success(units);
    }
}