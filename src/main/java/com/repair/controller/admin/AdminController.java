package com.repair.controller.admin;

//管理端

import com.repair.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("管理端测试成功");
    }
}
