package com.whd.cube.entity;

import lombok.Getter;
import lombok.Setter;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 比赛成绩表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("result")
public class MatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;

    /**
     * 赛事ID
     */
    @TableField("competition_id" )
    private Long competitionId;

    /**
     * 项目ID
     */
    @TableField("event_id" )
    private String eventId;

    /**
     * 轮次：Final(决赛), First(初赛), Second(复赛)
     */
    @TableField("round_type" )
    private String roundType;

    /**
     * 选手ID (关联 sys_user)
     */
    @TableField("user_id" )
    private Long userId;

    /**
     * 冗余展示ID (20251001)，用于快速显示
     */
    @TableField("user_display_id" )
    private String userDisplayId;

    /**
     * 第1次成绩
     */
    @TableField("value1" )
    private Integer value1;

    /**
     * 第2次成绩
     */
    @TableField("value2" )
    private Integer value2;

    /**
     * 第3次成绩
     */
    @TableField("value3" )
    private Integer value3;

    /**
     * 第4次成绩
     */
    @TableField("value4" )
    private Integer value4;

    /**
     * 第5次成绩
     */
    @TableField("value5" )
    private Integer value5;

    /**
     * 单次最佳 (Best)
     */
    @TableField("best" )
    private Integer best;

    /**
     * 去尾平均 (Average/Mean)
     */
    @TableField("average" )
    private Integer average;

    @TableField("create_time" )
    private LocalDateTime createTime;


}