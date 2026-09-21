package com.zhiyouxing.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 线路的每日行程：一天一行。
 *
 * <p>为什么单独一张表，而不是把行程塞进 travel_route.route_detail 一段文本：
 * 详情抽屉要按 Day 1 / Day 2 逐条排出来。一段纯文本要么让前端猜着切，要么靠
 * 「段落之间空一行」这种人工约定 —— 作者一处写乱，整段行程就糊在一起。
 * 拆成行之后，天数是数据（travel_route.days）、顺序是数据（day 升序），都不靠格式约定。
 *
 * <p>这张表和 travel_route_comment 一样**没有自己的控制器**，也不对外暴露增删改：
 * 读由 TravelRouteDayService.attachDaily 批量回填进 /travel_route/search 与
 * /travel_route/detail/{id} 的响应里。目前行程数据来自 zhiyouxing.sql 的种子数据，
 * 管理端只有线路主表的 CRUD（见 adminResources.js 的 travel_route），没有行程编辑器 ——
 * 要让导游自己录行程，得先给管理端补一个子表的编辑界面。
 */
@TableName("travel_route_day")
public class TravelRouteDayEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 线路id（travel_route.id）
     */
    private Long refId;

    /**
     * 第几天，从 1 开始
     */
    private Integer day;

    /**
     * 当天标题
     */
    private String title;

    /**
     * 当天安排
     */
    private String plan;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}
