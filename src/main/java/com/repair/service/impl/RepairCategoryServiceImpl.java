package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.RepairCategory;
import com.repair.mapper.RepairCategoryMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.service.RepairCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepairCategoryServiceImpl implements RepairCategoryService {

    @Autowired
    private RepairCategoryMapper repairCategoryMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Override
    @Transactional
    public RepairCategory addCategory(RepairCategory category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("类别名称不能为空");
        }
        String name = category.getName().trim();
        if (name.length() < 2 || name.length() > 20) {
            throw new BusinessException("类别名称长度必须在2-20位之间");
        }
        if (repairCategoryMapper.selectByName(name) != null) {
            throw new BusinessException("该报修类别已存在");
        }
        category.setName(name);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getStatus() != 0 && category.getStatus() != 1) {
            throw new BusinessException("状态值只能为0或1");
        }
        repairCategoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        if (id == null) {
            throw new BusinessException("类别ID不能为空");
        }
        RepairCategory category = repairCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("报修类别不存在");
        }
        Long used = repairOrderMapper.countOrdersByCategory(category.getName());
        if (used != null && used > 0) {
            throw new BusinessException("该类别已被" + used + "个报修单使用，无法删除，可改为停用");
        }
        repairCategoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public RepairCategory updateCategory(RepairCategory category) {
        if (category.getId() == null) {
            throw new BusinessException("类别ID不能为空");
        }
        RepairCategory existing = repairCategoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException("报修类别不存在");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("类别名称不能为空");
        }
        String name = category.getName().trim();
        if (name.length() < 2 || name.length() > 20) {
            throw new BusinessException("类别名称长度必须在2-20位之间");
        }
        RepairCategory sameName = repairCategoryMapper.selectByName(name);
        if (sameName != null && !sameName.getId().equals(category.getId())) {
            throw new BusinessException("该报修类别已存在");
        }
        if (category.getStatus() != null && category.getStatus() != 0 && category.getStatus() != 1) {
            throw new BusinessException("状态值只能为0或1");
        }
        category.setName(name);
        if (category.getSortOrder() == null) {
            category.setSortOrder(existing.getSortOrder());
        }
        if (category.getStatus() == null) {
            category.setStatus(existing.getStatus());
        }
        repairCategoryMapper.updateById(category);
        return repairCategoryMapper.selectById(category.getId());
    }

    @Override
    public RepairCategory getCategoryById(Integer id) {
        if (id == null) {
            throw new BusinessException("类别ID不能为空");
        }
        RepairCategory category = repairCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("报修类别不存在");
        }
        return category;
    }

    @Override
    public List<RepairCategory> listAllCategories() {
        return repairCategoryMapper.selectAll();
    }

    @Override
    public List<RepairCategory> listEnabledCategories() {
        return repairCategoryMapper.selectEnabled();
    }
}