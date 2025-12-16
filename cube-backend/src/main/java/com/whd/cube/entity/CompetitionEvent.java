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
 * 赛事开设项目表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("competition_event" )
public class CompetitionEvent implements Serializable {

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
     * 轮次数：1-决赛, 2-初赛+决赛, 3-三轮...
     */
    @TableField("round_count" )
    private Integer roundCount;


}