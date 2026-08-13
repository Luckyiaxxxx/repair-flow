package com.repair.controller.admin;

//管理端

import com.repair.common.Result;
import com.repair.common.ValidationGroups;
import com.repair.entity.Material;
import com.repair.service.DashboardService;
import com.repair.service.MaterialService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private MaterialService materialService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("管理端测试成功");
    }

    @PostMapping("/materials")
    public Result<Material> addMaterial(@Validated(ValidationGroups.Add.class)@RequestBody Material material){
        Material saved = materialService.addMaterial(material);
        return Result.success(saved);
    }

    @DeleteMapping("/materials/{id}")
    public Result<String> deleteMaterial(@PathVariable Integer id){
        materialService.deleteMaterial(id);
        return Result.success("删除成功");
    }

    @PutMapping("/materials")
    public Result<Material> updateMaterial(@Validated(ValidationGroups.Update.class) @RequestBody Material material){
        Material updated = materialService.updateMaterial(material);
        return Result.success(updated);
    }

    @GetMapping("/materials/{id}")
    public Result<Material> getMaterialById(@PathVariable Integer id){
        Material material = materialService.getMaterialById(id);
        return Result.success(material);
    }

    @GetMapping("/materials")
    public Result<List<Material>> listAllmaterials() {
        List<Material> materials = materialService.listAllMaterials();
        return Result.success(materials);
    }

    @GetMapping("/materials/low-stock")
    public Result<List<Material>> getLowStockMaterials(){
        List<Material> materials = materialService.getLowStockMaterials();
        return Result.success(materials);
    }

    @PutMapping("/materials/{id}/add-stock")
    public Result<String> addStock(@PathVariable Integer id ,@RequestParam Integer quantity){
        materialService.addStock(id,quantity);
        return Result.success("入库成功，增加"+quantity+"件");
    }

    @PutMapping("/materials/{id}/deduct-stock")
    public Result<String> deductStock(@PathVariable Integer id,@RequestParam Integer quantity){
        materialService.deductStock(id,quantity);
        return Result.success("出库成功，扣减"+quantity+"件");
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = dashboardService.getDashboardData();
        return Result.success(data);
    }
}
