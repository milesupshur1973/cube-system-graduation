package com.whd.cube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 赛事轮次配置表
 * 用于定义某场比赛、某个项目的具体轮次（初赛、复赛、决赛）及晋级规则
 * </p>
 *
 * @author WHD
 */
@Data
@TableName("competition_round")
public class CompetitionRound implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮次配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联赛事ID
     */
    @TableField("competition_id")
    private Long competitionId;

    /**
     * 关联项目ID (e.g. 333, 333bf)
     */
    @TableField("event_id")
    private String eventId;

    /**
     * 轮次顺序：1=初赛/R1, 2=复赛/R2, 3=决赛/Final
     */
    @TableField("round_order")
    private Integer roundOrder;

    /**
     * 轮次显示名称：初赛、复赛、决赛、R1、R2
     */
    @TableField("round_name")
    private String roundName;

    /**
     * 晋级下一轮的规则
     * 格式示例： "TOP-12" (前12名), "PCT-75" (前75%), NULL (决赛或不晋级)
     */
    @TableField("advancement_rule")
    private String advancementRule;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    // --- 下面是非数据库字段，仅用于业务方便 (Optional) ---

    /**
     * 赛制格式 (ao5/mo3/bo3)
     * 数据库不存，但业务层常需要展示，通过 Service 或 SQL 关联填充
     */
    @TableField(exist = false)
    private String format;
}