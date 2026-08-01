package com.repair.controller.owner;

//业主类

import com.repair.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("业主端测试成功");
    }
}
