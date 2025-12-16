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
import java.util.List;

/**
 * <p>
 * 赛事主表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("competition" )
public class Competition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事ID
     */
    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;

    /**
     * 赛事名称，如 2025年西安公开赛
     */
    @TableField("name" )
    private String name;

    /**
     * URL别名 (英文代号)，如 Xian-Open-2025
     */
    @TableField("slug" )
    private String slug;

    /**
     * 主办方用户ID (关联 sys_user.id)
     */
    @TableField("organizer_id" )
    private Long organizerId;

    /**
     * 举办省份
     */
    @TableField("province" )
    private String province;

    /**
     * 举办城市
     */
    @TableField("city" )
    private String city;

    /**
     * 详细地址/场馆名称
     */
    @TableField("location" )
    private String location;

    /**
     * 开始日期
     */
    @TableField("start_date" )
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date" )
    private LocalDate endDate;

    /**
     * [详情] 标签页内容 (HTML)
     */
    @TableField("content_description" )
    private String contentDescription;

    /**
     * [规则] 标签页内容 (HTML)
     */
    @TableField("content_rule" )
    private String contentRule;

    /**
     * 状态：0-审核中, 1-报名中(已发布), 2-进行中, 3-已结束, 4-已驳回
     */
    @TableField("status" )
    private Byte status;

    /**
     * 审核意见 (若驳回可填写原因)
     */
    @TableField("audit_msg" )
    private String auditMsg;

    /**
     * 报名开启时间
     */
    @TableField("reg_start_time" )
    private LocalDateTime regStartTime;

    /**
     * 报名截止时间
     */
    @TableField("reg_end_time" )
    private LocalDateTime regEndTime;

    /**
     * 选手人数限制
     */
    @TableField("competitor_limit" )
    private Integer competitorLimit;

    /**
     * 申请创建时间
     */
    @TableField("create_time" )
    private LocalDateTime createTime;

    @TableField("update_time" )
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<String> eventIds;

    @TableField(exist = false)
    private String organizerName;
}