package com.repair.controller.worker;

import com.repair.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//维修工端
@RestController
@RequestMapping("/api/worker")
public class WorkerController {
    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("维修工测试成功");
    }
}
