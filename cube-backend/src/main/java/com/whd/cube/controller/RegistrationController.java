package com.whd.cube.controller;

import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext; // 引入 UserContext
import com.whd.cube.dto.RegistrationDTO;
import com.whd.cube.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registration")
@CrossOrigin
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/competitors/{competitionId}")
    public Result getCompetitors(@PathVariable Long competitionId,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {

        return Result.success(registrationService.getCompetitorList(competitionId, page, size));
    }

    // 修改：去掉了 userId 参数，改用 UserContext.getUserId()
    @PostMapping("/apply")
    public Result apply(@RequestBody RegistrationDTO dto) {
        try {
            Long currentUserId = UserContext.getUserId(); // 从 Token 获取
            boolean success = registrationService.submitRegistration(dto, currentUserId);
            return Result.success("报名成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 修改：去掉了 userId 参数
    @GetMapping("/my")
    public Result getMyRegistration(@RequestParam Long competitionId) {
        Long currentUserId = UserContext.getUserId(); // 从 Token 获取
        RegistrationDTO dto = registrationService.getMyRegistration(competitionId, currentUserId);
        return Result.success(dto);
    }

    // 修改：去掉了 userId 参数
    @GetMapping("/my-list")
    public Result getMyList() {
        Long currentUserId = UserContext.getUserId(); // 从 Token 获取
        return Result.success(registrationService.getMyRegistrationList(currentUserId));
    }
    
    // 取消报名接口
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long competitionId) {
        try {
            Long currentUserId = UserContext.getUserId(); // 从 Token 获取
            registrationService.cancelRegistration(competitionId, currentUserId);
            return Result.success("取消报名成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}