package com.whd.cube.entity;

import lombok.Getter;
import lombok.Setter;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("sys_user" )
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物理主键，自增，从1001开始
     */
    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;

    /**
     * 展示用ID (拟真WCAID)，格式：年份+ID，如 20251001
     */
    @TableField("display_id" )
    private String displayId;

    /**
     * 邮箱，登录账号
     */
    @TableField("email" )
    private String email;

    /**
     * 加密密码 (Bcrypt)
     */
    @TableField("password" )
    private String password;

    /**
     * 真实姓名 (比赛用)
     */
    @TableField("name" )
    private String name;

    /**
     * 头像地址
     */
    @TableField("avatar_url" )
    private String avatarUrl;

    /**
     * 性别：M-男, F-女
     */
    @TableField("gender" )
    private String gender;

    /**
     * 省份 (用于省份排名)
     */
    @TableField("province" )
    private String province;

    /**
     * 城市 (用于同城统计)
     */
    @TableField("city" )
    private String city;

    /**
     * 角色：user-普通用户/主办方, admin-系统管理员
     */
    @TableField("role" )
    private String role;

    /**
     * 注册时间
     */
    @TableField("create_time" )
    private LocalDateTime createTime;


}