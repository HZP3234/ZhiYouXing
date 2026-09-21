package com.zhiyouxing.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 旅游攻略标签（字典表）。
 *
 * <p>形状照 room_type / attraction_type / route_type 那套「分类字典」的先例：
 * id + add_time + 一个值列 + 值列唯一键。区别只在于它是**作者在写作页里随手建的**，
 * 没有对应的增删改接口（管理端也不登记它），所以这里也不需要 image 列。
 *
 * <p>tag_name 上的唯一键是并发安全的依据：两个人同时新建同名标签时，
 * 后到的 INSERT IGNORE 会被唯一键吃掉，TravelGuideTagServiceImpl 再查一次拿 id。
 */
@TableName("travel_guide_tag")
public class TravelGuideTagEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}
