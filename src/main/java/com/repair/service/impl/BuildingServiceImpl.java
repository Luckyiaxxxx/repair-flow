package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Building;
import com.repair.entity.BuildingUnit;
import com.repair.mapper.BuildingMapper;
import com.repair.mapper.BuildingUnitMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private BuildingUnitMapper buildingUnitMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    private void checkStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException("状态值只能为0或1");
        }
    }

    // ==================== 楼栋 ====================

    @Override
    @Transactional
    public Building addBuilding(Building building) {
        if (building.getName() == null || building.getName().trim().isEmpty()) {
            throw new BusinessException("楼栋名称不能为空");
        }
        String name = building.getName().trim();
        if (name.length() > 50) {
            throw new BusinessException("楼栋名称长度必须在1-50位之间");
        }
        if (buildingMapper.selectByName(name) != null) {
            throw new BusinessException("该楼栋已存在");
        }
        checkStatus(building.getStatus());
        building.setName(name);
        if (building.getSortOrder() == null) {
            building.setSortOrder(0);
        }
        if (building.getStatus() == null) {
            building.setStatus(1);
        }
        buildingMapper.insert(building);
        return building;
    }

    @Override
    @Transactional
    public void deleteBuilding(Integer id) {
        if (id == null) {
            throw new BusinessException("楼栋ID不能为空");
        }
        Building building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }
        if (buildingUnitMapper.countByBuildingId(id) > 0) {
            throw new BusinessException("该楼栋下存在单元，请先删除单元");
        }
        Long used = repairOrderMapper.countOrdersByBuilding(building.getName());
        if (used != null && used > 0) {
            throw new BusinessException("该楼栋已被" + used + "个报修单使用，无法删除，可改为停用");
        }
        buildingMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Building updateBuilding(Building building) {
        if (building.getId() == null) {
            throw new BusinessException("楼栋ID不能为空");
        }
        Building existing = buildingMapper.selectById(building.getId());
        if (existing == null) {
            throw new BusinessException("楼栋不存在");
        }
        if (building.getName() == null || building.getName().trim().isEmpty()) {
            throw new BusinessException("楼栋名称不能为空");
        }
        String name = building.getName().trim();
        if (name.length() > 50) {
            throw new BusinessException("楼栋名称长度必须在1-50位之间");
        }
        Building sameName = buildingMapper.selectByName(name);
        if (sameName != null && !sameName.getId().equals(building.getId())) {
            throw new BusinessException("该楼栋已存在");
        }
        checkStatus(building.getStatus());
        building.setName(name);
        if (building.getSortOrder() == null) {
            building.setSortOrder(existing.getSortOrder());
        }
        if (building.getStatus() == null) {
            building.setStatus(existing.getStatus());
        }
        buildingMapper.updateById(building);
        return buildingMapper.selectById(building.getId());
    }

    @Override
    public Building getBuildingById(Integer id) {
        if (id == null) {
            throw new BusinessException("楼栋ID不能为空");
        }
        Building building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }
        return building;
    }

    @Override
    public List<Building> listAllBuildings() {
        return buildingMapper.selectAll();
    }

    // ==================== 单元 ====================

    @Override
    @Transactional
    public BuildingUnit addUnit(BuildingUnit unit) {
        if (unit.getBuildingId() == null) {
            throw new BusinessException("所属楼栋ID不能为空");
        }
        Building building = buildingMapper.selectById(unit.getBuildingId());
        if (building == null) {
            throw new BusinessException("所属楼栋不存在");
        }
        if (unit.getName() == null || unit.getName().trim().isEmpty()) {
            throw new BusinessException("单元名称不能为空");
        }
        String name = unit.getName().trim();
        if (name.length() > 50) {
            throw new BusinessException("单元名称长度必须在1-50位之间");
        }
        if (buildingUnitMapper.selectByBuildingIdAndName(unit.getBuildingId(), name) != null) {
            throw new BusinessException("该楼栋下已存在此单元");
        }
        checkStatus(unit.getStatus());
        unit.setName(name);
        if (unit.getSortOrder() == null) {
            unit.setSortOrder(0);
        }
        if (unit.getStatus() == null) {
            unit.setStatus(1);
        }
        buildingUnitMapper.insert(unit);
        return buildingUnitMapper.selectById(unit.getId());
    }

    @Override
    @Transactional
    public void deleteUnit(Integer id) {
        if (id == null) {
            throw new BusinessException("单元ID不能为空");
        }
        BuildingUnit unit = buildingUnitMapper.selectById(id);
        if (unit == null) {
            throw new BusinessException("单元不存在");
        }
        Building building = buildingMapper.selectById(unit.getBuildingId());
        String buildingName = building != null ? building.getName() : "";
        Long used = repairOrderMapper.countOrdersByBuildingAndUnit(buildingName, unit.getName());
        if (used != null && used > 0) {
            throw new BusinessException("该单元已被" + used + "个报修单使用，无法删除，可改为停用");
        }
        buildingUnitMapper.deleteById(id);
    }

    @Override
    @Transactional
    public BuildingUnit updateUnit(BuildingUnit unit) {
        if (unit.getId() == null) {
            throw new BusinessException("单元ID不能为空");
        }
        BuildingUnit existing = buildingUnitMapper.selectById(unit.getId());
        if (existing == null) {
            throw new BusinessException("单元不存在");
        }
        if (unit.getName() == null || unit.getName().trim().isEmpty()) {
            throw new BusinessException("单元名称不能为空");
        }
        String name = unit.getName().trim();
        if (name.length() > 50) {
            throw new BusinessException("单元名称长度必须在1-50位之间");
        }
        Integer buildingId = unit.getBuildingId() != null ? unit.getBuildingId() : existing.getBuildingId();
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException("所属楼栋不存在");
        }
        BuildingUnit sameName = buildingUnitMapper.selectByBuildingIdAndName(buildingId, name);
        if (sameName != null && !sameName.getId().equals(unit.getId())) {
            throw new BusinessException("该楼栋下已存在此单元");
        }
        checkStatus(unit.getStatus());
        unit.setBuildingId(buildingId);
        unit.setName(name);
        if (unit.getSortOrder() == null) {
            unit.setSortOrder(existing.getSortOrder());
        }
        if (unit.getStatus() == null) {
            unit.setStatus(existing.getStatus());
        }
        buildingUnitMapper.updateById(unit);
        return buildingUnitMapper.selectById(unit.getId());
    }

    @Override
    public BuildingUnit getUnitById(Integer id) {
        if (id == null) {
            throw new BusinessException("单元ID不能为空");
        }
        BuildingUnit unit = buildingUnitMapper.selectById(id);
        if (unit == null) {
            throw new BusinessException("单元不存在");
        }
        return unit;
    }

    @Override
    public List<BuildingUnit> listUnits(Integer buildingId) {
        if (buildingId != null) {
            return buildingUnitMapper.selectByBuildingId(buildingId);
        }
        return buildingUnitMapper.selectAll();
    }

    // ==================== 业主端选择数据 ====================

    @Override
    public List<Map<String, Object>> listEnabledBuildingsWithUnits() {
        List<Building> buildings = buildingMapper.selectEnabled();
        List<BuildingUnit> allUnits = buildingUnitMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Building building : buildings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", building.getId());
            item.put("name", building.getName());
            List<Map<String, Object>> units = new ArrayList<>();
            for (BuildingUnit unit : allUnits) {
                if (unit.getBuildingId().equals(building.getId()) && unit.getStatus() != null && unit.getStatus() == 1) {
                    Map<String, Object> u = new LinkedHashMap<>();
                    u.put("id", unit.getId());
                    u.put("name", unit.getName());
                    units.add(u);
                }
            }
            item.put("units", units);
            result.add(item);
        }
        return result;
    }
}