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
 * 新闻公告表
 * </p>
 *
 * @author WHD
 */
@Getter
@Setter
@TableName("article" )
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    @TableField("title" )
    private String title;

    /**
     * 分类：news-新闻, notice-通知
     */
    @TableField("category" )
    private String category;

    /**
     * 公告内容 (HTML)
     */
    @TableField("content" )
    private String content;

    /**
     * 发布人ID
     */
    @TableField("user_id" )
    private Long userId;

    /**
     * 发布时间
     */
    @TableField("publish_time" )
    private LocalDateTime publishTime;


}