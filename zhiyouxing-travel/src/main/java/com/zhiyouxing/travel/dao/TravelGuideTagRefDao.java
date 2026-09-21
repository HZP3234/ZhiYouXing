package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyouxing.travel.entity.TravelGuideTagRefEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 攻略↔标签关联表。
 *
 * <p>只有写入需要自定义 SQL（INSERT IGNORE）；读关联一律用 BaseMapper 的
 * selectList + QueryWrapper（见 TravelGuideTagServiceImpl），所以这里没有多余的查询方法 ——
 * 也就没有一条手写 SQL 需要维护。
 */
public interface TravelGuideTagRefDao extends BaseMapper<TravelGuideTagRefEntity> {

    /**
     * 挂一条关联。库里 (guide_id, tag_id) 有唯一键，重复插入被 IGNORE 吃掉。
     */
    @Insert("INSERT IGNORE INTO travel_guide_tag_ref (guide_id, tag_id) VALUES (#{guideId}, #{tagId})")
    int insertIgnore(@Param("guideId") Long guideId, @Param("tagId") Long tagId);
}
