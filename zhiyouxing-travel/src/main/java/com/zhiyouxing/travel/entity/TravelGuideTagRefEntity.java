package com.zhiyouxing.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 攻略与标签的关联行（一篇攻略挂多个标签，所以是标准的多对多中间表）。
 *
 * <p>库里 (guide_id, tag_id) 上有唯一键，写入一律走 INSERT IGNORE：
 * 重复挂同一个标签不会插出两行，也不需要先查再插。
 *
 * <p>这张表**没有自己的控制器**：标签页的字典走 /travel_guide_tag/list，
 * 按标签查攻略走 /travel_guide/search?tagId=，写关联走 /travel_guide/save|update，
 * 都由 TravelGuideTagService 统一收口，外面看不到这张表。
 */
@TableName("travel_guide_tag_ref")
public class TravelGuideTagRefEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 攻略id
     */
    private Long guideId;

    /**
     * 标签id
     */
    private Long tagId;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGuideId() {
        return guideId;
    }

    public void setGuideId(Long guideId) {
        this.guideId = guideId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}
