package com.repair.controller.admin;

//管理端：维修工档案（技能标签、在岗状态、服务区域）

import com.repair.common.Result;
import com.repair.dto.WorkerProfileRequest;
import com.repair.entity.User;
import com.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/workers")
public class WorkerProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<User>> listWorkers(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer onDuty) {
        List<User> workers = userService.listWorkers(skill, onDuty);
        return Result.success(workers);
    }

    @GetMapping("/{id}")
    public Result<User> getWorkerDetail(@PathVariable Integer id) {
        User worker = userService.getWorkerDetail(id);
        return Result.success(worker);
    }

    @PutMapping("/{id}")
    public Result<User> updateWorkerProfile(@PathVariable Integer id, @RequestBody WorkerProfileRequest request) {
        User updated = userService.updateWorkerProfile(id, request.getSkill(), request.getOnDuty(),
                request.getServiceArea(), request.getMaxWorkload());
        return Result.success(updated);
    }
}