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
 * 魔方项目字典表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("event" )
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目代码 (PK)，如 333, 222, 333oh
     */
    @TableId(value = "id" , type = IdType.AUTO)
    private String id;

    /**
     * 项目名称，如 三阶魔方
     */
    @TableField("name" )
    private String name;

    /**
     * 还原格式：ao5 (5次去头尾), mo3 (3次取平均), bo3 (3次取最佳)
     */
    @TableField("format" )
    private String format;


    /**
     * 是否官方正赛项目：1-是, 0-趣味项目
     */
    @TableField("is_official" )
    private Boolean isOfficial;


}