package com.repair.controller.common;

//公共端：按类型读取启用中的字典项（各角色表单选择用）

import com.repair.common.Result;
import com.repair.entity.SysDict;
import com.repair.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/common/dicts")
public class CommonDictController {

    @Autowired
    private SysDictService sysDictService;

    @GetMapping("/{type}")
    public Result<List<SysDict>> listEnabledDicts(@PathVariable String type) {
        List<SysDict> dicts = sysDictService.listEnabledDicts(type);
        return Result.success(dicts);
    }
}