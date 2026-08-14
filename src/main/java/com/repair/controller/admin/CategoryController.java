package com.repair.controller.admin;

//管理端：报修类别字典管理

import com.repair.common.Result;
import com.repair.common.ValidationGroups;
import com.repair.entity.RepairCategory;
import com.repair.service.RepairCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    @Autowired
    private RepairCategoryService repairCategoryService;

    @PostMapping
    public Result<RepairCategory> addCategory(@Validated(ValidationGroups.Add.class) @RequestBody RepairCategory category) {
        RepairCategory saved = repairCategoryService.addCategory(category);
        return Result.success(saved);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Integer id) {
        repairCategoryService.deleteCategory(id);
        return Result.success("删除成功");
    }

    @PutMapping
    public Result<RepairCategory> updateCategory(@Validated(ValidationGroups.Update.class) @RequestBody RepairCategory category) {
        RepairCategory updated = repairCategoryService.updateCategory(category);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    public Result<RepairCategory> getCategoryById(@PathVariable Integer id) {
        RepairCategory category = repairCategoryService.getCategoryById(id);
        return Result.success(category);
    }

    @GetMapping
    public Result<List<RepairCategory>> listAllCategories() {
        List<RepairCategory> categories = repairCategoryService.listAllCategories();
        return Result.success(categories);
    }
}