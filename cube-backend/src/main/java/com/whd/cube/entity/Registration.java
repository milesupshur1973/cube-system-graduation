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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 选手报名记录表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("registration" )
public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;

    /**
     * 赛事ID
     */
    @TableField("competition_id" )
    private Long competitionId;

    /**
     * 选手ID
     */
    @TableField("user_id" )
    private Long userId;

    /**
     * 冗余选手展示ID，方便查询
     */
    @TableField("user_display_id" )
    private String userDisplayId;

    /**
     * 冗余选手姓名，方便展示
     */
    @TableField("user_name" )
    private String userName;

    /**
     * 状态：0-待审核, 1-报名成功/已通过, 2-已取消
     */
    @TableField("status" )
    private Byte status;

    /**
     * 报名时间
     */
    @TableField("create_time" )
    private LocalDateTime createTime;

    //配合连表查询的属性
    @TableField(exist = false)
    private String userGender;

    @TableField(exist = false)
    private String userProvince;

    @TableField(exist = false)
    private String eventIdsStr; // 数据库查出来的 "333,444"

    @TableField(exist = false)
    private List<String> eventIds; // 给前端用的 List

    /**
     * MyBatis 查询出 eventIdsStr 后会自动调用这个 setter
     * 我们在这里顺便把 List 给转好
     */
    public void setEventIdsStr(String eventIdsStr) {
        this.eventIdsStr = eventIdsStr;
        if (eventIdsStr != null && !eventIdsStr.isEmpty()) {
            this.eventIds = Arrays.asList(eventIdsStr.split(","));
        } else {
            this.eventIds = Collections.emptyList();
        }
    }
}