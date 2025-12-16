package com.whd.cube.controller;

import com.whd.cube.common.Result;
import com.whd.cube.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 魔方项目字典表 前端控制器
 * </p>
 *
 * @author WHD
 */
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * 获取所有项目列表 (用于前端字典缓存)
     */
    @GetMapping("/list")
    public Result list() {
        return Result.success(eventService.list());
    }
}
