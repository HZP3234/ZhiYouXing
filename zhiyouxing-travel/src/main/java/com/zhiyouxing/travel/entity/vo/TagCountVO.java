package com.zhiyouxing.travel.entity.vo;

import java.io.Serializable;

/**
 * 筛选栏用的标签 + 篇数。
 *
 * <p>不是表映射，没有 @TableName —— 它只由 TravelGuideTagDao.selectTagsWithCount()
 * 的那条 GROUP BY 查询填充。列表页那排标签 chips 一次性拿全量，
 * 而不是每个标签打一次「有几篇」的接口。
 */
public class TagCountVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标签id（前端点它传给 /travel_guide/search?tagId=）
     */
    private Long tagId;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 这个标签下有几篇攻略（LEFT JOIN 来的，没人用过的标签是 0）
     */
    private Integer guideCount;

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Integer getGuideCount() {
        return guideCount;
    }

    public void setGuideCount(Integer guideCount) {
        this.guideCount = guideCount;
    }
}
