package com.whd.cube.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext;
import com.whd.cube.entity.User;
import com.whd.cube.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author WHD
 */
@RestController
@RequestMapping("/user")
@CrossOrigin // 允许跨域，防止前端请求报错
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    // 获取用户详情（用于个人主页）
    @GetMapping("/{id}")
    public Result getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    // DisplayId 获取公开的用户信息
    @GetMapping("/public/{displayId}")
    public Result getPublicInfo(@PathVariable String displayId) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("display_id", displayId);
        User user = userService.getOne(query);
        if (user == null) return Result.error("选手不存在");

        // 脱敏处理，不返回密码
        user.setPassword(null);
        return Result.success(user);
    }

    // 选手列表查询 (支持分页 + 筛选)
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String gender,
                       @RequestParam(required = false) String province) {

        // 1. 构建分页对象
        Page<User> pageInfo = new Page<>(page, size);

        // 2. 构建查询条件
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 姓名/WCAID 模糊查询 (name 字段匹配)
        if (StringUtils.hasText(name)) {
            // allowing search by real name OR displayId
            wrapper.and(w -> w.like("name", name).or().like("display_id", name));
        }
        // 性别精确查询
        if (StringUtils.hasText(gender)) {
            wrapper.eq("gender", gender);
        }
        // 省份精确查询
        if (StringUtils.hasText(province)) {
            wrapper.eq("province", province);
        }

        // 按注册时间倒序 (新的在前面)
        wrapper.orderByDesc("create_time");

        // 3. 执行查询
        Page<User> userPage = userService.page(pageInfo, wrapper);

        // 4. 脱敏处理 (把密码设为 null，防止泄露)
        userPage.getRecords().forEach(u -> u.setPassword(null));

        return Result.success(userPage);
    }

    /**
     * 更新个人头像
     */
    @PostMapping("/update")
    public Result update(@RequestBody User inputUser) {
        // 获取当前登录用户 ID
        Long currentUserId = UserContext.getUserId();

        User updateEntity = new User();
        updateEntity.setId(currentUserId);

        updateEntity.setAvatarUrl(inputUser.getAvatarUrl());

        boolean success = userService.updateById(updateEntity);

        if (success) {
            // 更新成功后，查询最新的完整用户信息返回给前端（用于更新页面显示的头像和名字）
            User latestUser = userService.getById(currentUserId);
            latestUser.setPassword(null); // 脱敏
            return Result.success(latestUser);
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/update-password")
    public Result updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        return userService.updatePassword(oldPassword, newPassword);
    }

    /**
     * 修改用户角色 (仅管理员可用)
     */
    @PostMapping("/role")
    public Result updateRole(@RequestBody Map<String, Object> params) {
        // 1. 鉴权：只有管理员能调用
        Long currentUserId = UserContext.getUserId();
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error("权限不足");
        }

        // 2. 获取参数
        Long targetUserId = Long.valueOf(params.get("userId").toString());
        String newRole = (String) params.get("role");

        // 3. 校验：不能修改自己的角色 (防止管理员把自己降级后失去权限，这就尴尬了)
        if (targetUserId.equals(currentUserId)) {
            return Result.error("不能修改自己的角色");
        }

        // 4. 执行更新
        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setRole(newRole);

        boolean success = userService.updateById(targetUser);
        return success ? Result.success("角色设置成功") : Result.error("设置失败");
    }
}
