package com.whd.cube.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext;
import com.whd.cube.entity.User;
import com.whd.cube.mapper.UserMapper;
import com.whd.cube.service.UserService;
import com.whd.cube.utils.JwtUtils; // 1. 引入 JwtUtils
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author WHD
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 登录逻辑
    @Override
    public Result login(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", user.getEmail());
        User dbUser = getOne(wrapper);

        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            return Result.error("密码错误");
        }


        // 3. Jwt生成 Token (传入用户ID和角色)
        String token = jwtUtils.generateToken(dbUser.getId(), dbUser.getRole());

        // 4. 构建返回数据 (Token + 用户基本信息)
        // 为什么要返回 user？因为前端展示需要头像和名字，只给 Token 前端还得自己解密太麻烦
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        dbUser.setPassword(null); // 记得要把密码抹除
        data.put("user", dbUser);

        return Result.success(data);
        // ==================== JWT 改造结束 ====================
    }

    // 注册逻辑 (保持不变)
    @Override
    public Result register(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", user.getEmail());
        if (count(wrapper) > 0) {
            return Result.error("邮箱已被注册");
        }

        // 1. 加密密码
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("user");

        // 2. 先保存以获取自增ID
        save(user);
        // 3. 生成拟真ID (年份 + ID)
        String year = String.valueOf(Year.now().getValue());
        String displayId = year + user.getId();
        user.setDisplayId(displayId);

        // 4. 更新
        updateById(user);

        return Result.success(user);
    }

    // 修改密码
    @Override
    public Result updatePassword(String oldPassword, String newPassword) {
        // 1. 获取当前用户
        Long userId = UserContext.getUserId();
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 2. 校验旧密码 (必须使用 matches 方法比对加密密码)
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误，请重新输入");
        }

        // 3. 校验新密码长度 (简单校验一下)
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        // 4. 加密新密码并保存
        String newEncodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(newEncodedPassword);

        updateById(user);

        return Result.success("密码修改成功");
    }
}