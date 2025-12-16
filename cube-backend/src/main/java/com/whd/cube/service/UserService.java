package com.whd.cube.service;

import com.whd.cube.common.Result;
import com.whd.cube.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author WHD
 */
public interface UserService extends IService<User> {

    // 定义登录方法
    Result login(User user);

    // 定义注册方法
    Result register(User user);

    // 定义修改密码方法
    Result updatePassword(String oldPassword, String newPassword);
}