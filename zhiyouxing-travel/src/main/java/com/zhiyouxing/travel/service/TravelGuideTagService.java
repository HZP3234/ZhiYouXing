package com.zhiyouxing.travel.service;

import com.zhiyouxing.travel.entity.view.TravelGuideView;
import com.zhiyouxing.travel.entity.vo.TagCountVO;

import java.util.List;

/**
 * 攻略标签：字典表与关联表两张表在这里收口。
 *
 * <p>不单独给关联表建 Service —— 它对外没有独立语义，
 * 无论是「有哪些标签」「这篇有哪些标签」「按标签找攻略」还是「重挂标签」，
 * 调用方（TravelGuideController / TravelGuideTagController）要的都是一个动作。
 */
public interface TravelGuideTagService {

    /**
     * 一篇攻略最多挂几个标签。前端 el-select 也按这个数限（multiple-limit）。
     */
    int MAX_TAGS_PER_GUIDE = 8;

    /**
     * 筛选栏的标签 + 篇数，热门优先。
     */
    List<TagCountVO> listWithCount();

    /**
     * 把标签名回填到列表页那批 View 上。
     *
     * <p>两条查询 + Java 归并（本页 guide_id 一次 IN 取关联行，再按 tag_id 一次 IN 取名），
     * 不写自定义 SQL、不拼串，也不会 N+1。
     */
    void attachTagNames(List<TravelGuideView> guides);

    /**
     * 某一篇的标签名列表，给详情页用。
     */
    List<String> selectTagNamesByGuide(Long guideId);

    /**
     * 某个标签下的攻略 id 列表。返回空集合是常态（这个标签还没人用），
     * 调用方必须判空 —— MyBatis-Plus 的 in() 不做空判，空集合会拼出 `id IN ()` 报语法错。
     */
    List<Long> selectGuideIdsByTag(Long tagId);

    /**
     * 整体替换某一篇的标签。
     *
     * @param tagNames null = 什么都不做（局部更新不带 tagNames 时别误清）、
     *                 空列表 = 清空标签、非空 = 先删旧关联再按名单重建（缺的标签自动新建）
     */
    void replaceGuideTags(Long guideId, List<String> tagNames);

    /**
     * 攻略被删除时连带清掉它的关联行，别留孤儿数据在关联表里。
     */
    void removeByGuideIds(List<Long> guideIds);

    /**
     * 清洗标签名：去首尾空白、丢掉空的、按名字去重、单个名字截到 200（跟列宽一致）。
     *
     * <p>公开出来是为了让 controller 能在**落库之前**先用它算个数、把「标签太多」
     * 挡在建攻略之前（不然会出现攻略已存、标签被拒的半截状态）。
     *
     * @return null 原样返回 null（保留「不动标签」的语义）
     */
    List<String> normalizeTagNames(List<String> raw);
}
