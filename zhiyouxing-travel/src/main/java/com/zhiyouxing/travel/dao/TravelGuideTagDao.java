package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyouxing.travel.entity.TravelGuideTagEntity;
import com.zhiyouxing.travel.entity.vo.TagCountVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 旅游攻略标签（字典表）。
 *
 * <p>放在本模块自己的 dao 包，和 TravelGuideDao 同居一处 —— 各模块的
 * @MapperScan 只扫自己的包（TravelGuideApplication 扫 com.zhiyouxing.travel.dao
 * 与 com.zhiyouxing.common.dao），所以这里零额外配置。
 */
public interface TravelGuideTagDao extends BaseMapper<TravelGuideTagEntity> {

    /**
     * 筛选栏的标签 + 篇数：一条 GROUP BY 出全量。
     * 用 LEFT JOIN，没人用过的标签也要出现在 chips 里（篇数是 0），
     * 否则作者新建的标签会在筛选栏里凭空消失。
     * 排序按「热门优先」，同篇数时按 id（即创建先后）稳定下来。
     */
    @Select("SELECT t.id AS tagId, t.tag_name AS tagName, COUNT(r.id) AS guideCount "
            + "FROM travel_guide_tag t "
            + "LEFT JOIN travel_guide_tag_ref r ON r.tag_id = t.id "
            + "GROUP BY t.id, t.tag_name "
            + "ORDER BY guideCount DESC, t.id ASC")
    List<TagCountVO> selectTagsWithCount();

    /**
     * 新建标签。用 INSERT IGNORE 而不是先查后插：两个人同时提交同一个新标签时，
     * 后到的这一次会被 tag_name 唯一键吃掉而不抛异常，调用方插入后再查一次拿 id。
     */
    @Insert("INSERT IGNORE INTO travel_guide_tag (tag_name) VALUES (#{tagName})")
    int insertIgnore(@Param("tagName") String tagName);

    /**
     * 按名字查 id。名字上有唯一键，所以最多一行。
     */
    @Select("SELECT id FROM travel_guide_tag WHERE tag_name = #{tagName} LIMIT 1")
    Long selectIdByName(@Param("tagName") String tagName);
}
