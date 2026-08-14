package com.repair.service;

import com.repair.entity.RepairCategory;

import java.util.List;

public interface RepairCategoryService {

    RepairCategory addCategory(RepairCategory category);

    void deleteCategory(Integer id);

    RepairCategory updateCategory(RepairCategory category);

    RepairCategory getCategoryById(Integer id);

    List<RepairCategory> listAllCategories();

    List<RepairCategory> listEnabledCategories();
}