package com.repair.controller.dispatcher;

import com.repair.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//客服端
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherController {

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("客服端测试成功");

    }
}
