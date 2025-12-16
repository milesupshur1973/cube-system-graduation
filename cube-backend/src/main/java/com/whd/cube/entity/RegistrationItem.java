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
 * 报名项目明细表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("registration_item" )
public class RegistrationItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;

    /**
     * 关联报名主表ID
     */
    @TableField("registration_id" )
    private Long registrationId;

    /**
     * 项目ID
     */
    @TableField("event_id" )
    private String eventId;


}