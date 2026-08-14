package com.repair.controller.admin;

//管理端：数据字典统一管理

import com.repair.common.Result;
import com.repair.common.ValidationGroups;
import com.repair.entity.SysDict;
import com.repair.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dicts")
public class DictController {

    @Autowired
    private SysDictService sysDictService;

    @PostMapping
    public Result<SysDict> addDict(@Validated(ValidationGroups.Add.class) @RequestBody SysDict dict) {
        SysDict saved = sysDictService.addDict(dict);
        return Result.success(saved);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDict(@PathVariable Integer id) {
        sysDictService.deleteDict(id);
        return Result.success("删除成功");
    }

    @PutMapping
    public Result<SysDict> updateDict(@Validated(ValidationGroups.Update.class) @RequestBody SysDict dict) {
        SysDict updated = sysDictService.updateDict(dict);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    public Result<SysDict> getDictById(@PathVariable Integer id) {
        SysDict dict = sysDictService.getDictById(id);
        return Result.success(dict);
    }

    @GetMapping
    public Result<List<SysDict>> listDicts(@RequestParam(required = false) String type) {
        List<SysDict> dicts = sysDictService.listDicts(type);
        return Result.success(dicts);
    }

    @GetMapping("/types")
    public Result<List<String>> listDictTypes() {
        List<String> types = sysDictService.listDictTypes();
        return Result.success(types);
    }
}