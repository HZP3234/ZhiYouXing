package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.travel.dao.TravelGuideTagDao;
import com.zhiyouxing.travel.dao.TravelGuideTagRefDao;
import com.zhiyouxing.travel.entity.TravelGuideTagEntity;
import com.zhiyouxing.travel.entity.TravelGuideTagRefEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideView;
import com.zhiyouxing.travel.entity.vo.TagCountVO;
import com.zhiyouxing.travel.service.TravelGuideTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 攻略标签。
 *
 * <p>设计上的两条取舍：
 * <ul>
 *   <li><b>关联表的读全用 BaseMapper + QueryWrapper，一条手写 SQL 都没有。</b>
 *       只有写入需要 INSERT IGNORE（去吃 (guide_id, tag_id) 唯一键），才写在 DAO 上。</li>
 *   <li><b>标签名 → id 的解析只在写入时做一次</b>，之后全靠 id 关联。
 *       所以「青海湖」这个标签改名/删除只影响字典表，不会波及已挂好的攻略。</li>
 * </ul>
 *
 * <p>注意查询里所有 in() 之前都必须判空：MyBatis-Plus 的 in() 不做空判，
 * 空集合会拼出 `id IN ()` 直接 MySQL 语法错误。
 */
@Service("travelGuideTagService")
public class TravelGuideTagServiceImpl implements TravelGuideTagService {

    /**
     * 与 travel_guide_tag.tag_name 的列宽一致（varchar(200)，MySQL 按字符计）
     */
    private static final int MAX_TAG_NAME_LENGTH = 200;

    @Autowired
    private TravelGuideTagDao travelGuideTagDao;

    @Autowired
    private TravelGuideTagRefDao travelGuideTagRefDao;

    @Override
    public List<TagCountVO> listWithCount() {
        return travelGuideTagDao.selectTagsWithCount();
    }

    @Override
    public void attachTagNames(List<TravelGuideView> guides) {
        if (guides == null || guides.isEmpty()) {
            return;
        }
        List<Long> guideIds = new ArrayList<>();
        for (TravelGuideView guide : guides) {
            if (guide != null && guide.getId() != null) {
                guideIds.add(guide.getId());
            }
        }
        // 先把每个 View 的标签置成空列表：宁可显示「暂无标签」也不要吐 null 给前端 split
        Map<Long, List<String>> namesByGuide = new HashMap<>();
        for (TravelGuideView guide : guides) {
            if (guide != null) {
                guide.setTagNames(new ArrayList<>());
            }
        }
        if (guideIds.isEmpty()) {
            return;
        }

        List<TravelGuideTagRefEntity> refs = travelGuideTagRefDao.selectList(
                new QueryWrapper<TravelGuideTagRefEntity>().in("guide_id", guideIds));
        if (refs.isEmpty()) {
            return;
        }

        Set<Long> tagIds = new HashSet<>();
        for (TravelGuideTagRefEntity ref : refs) {
            if (ref.getTagId() != null) {
                tagIds.add(ref.getTagId());
            }
        }
        Map<Long, String> nameById = new HashMap<>();
        if (!tagIds.isEmpty()) {
            for (TravelGuideTagEntity tag : travelGuideTagDao.selectList(
                    new QueryWrapper<TravelGuideTagEntity>().in("id", tagIds))) {
                nameById.put(tag.getId(), tag.getTagName());
            }
        }

        for (TravelGuideTagRefEntity ref : refs) {
            String name = nameById.get(ref.getTagId());
            // 标签被清理掉但关联没清干净时跳过，别往页面上送一个 null
            if (name == null) {
                continue;
            }
            if (!namesByGuide.containsKey(ref.getGuideId())) {
                namesByGuide.put(ref.getGuideId(), new ArrayList<>());
            }
            namesByGuide.get(ref.getGuideId()).add(name);
        }

        for (TravelGuideView guide : guides) {
            if (guide == null) {
                continue;
            }
            List<String> names = namesByGuide.get(guide.getId());
            guide.setTagNames(names == null ? new ArrayList<>() : names);
        }
    }

    @Override
    public List<String> selectTagNamesByGuide(Long guideId) {
        List<String> names = new ArrayList<>();
        if (guideId == null) {
            return names;
        }
        List<TravelGuideTagRefEntity> refs = travelGuideTagRefDao.selectList(
                new QueryWrapper<TravelGuideTagRefEntity>().eq("guide_id", guideId));
        if (refs.isEmpty()) {
            return names;
        }
        Set<Long> tagIds = new HashSet<>();
        for (TravelGuideTagRefEntity ref : refs) {
            if (ref.getTagId() != null) {
                tagIds.add(ref.getTagId());
            }
        }
        if (tagIds.isEmpty()) {
            return names;
        }
        Map<Long, String> nameById = new HashMap<>();
        for (TravelGuideTagEntity tag : travelGuideTagDao.selectList(
                new QueryWrapper<TravelGuideTagEntity>().in("id", tagIds))) {
            nameById.put(tag.getId(), tag.getTagName());
        }
        for (TravelGuideTagRefEntity ref : refs) {
            String name = nameById.get(ref.getTagId());
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    @Override
    public List<Long> selectGuideIdsByTag(Long tagId) {
        List<Long> guideIds = new ArrayList<>();
        if (tagId == null) {
            return guideIds;
        }
        for (TravelGuideTagRefEntity ref : travelGuideTagRefDao.selectList(
                new QueryWrapper<TravelGuideTagRefEntity>().eq("tag_id", tagId))) {
            if (ref.getGuideId() != null) {
                guideIds.add(ref.getGuideId());
            }
        }
        return guideIds;
    }

    @Override
    @Transactional
    public void replaceGuideTags(Long guideId, List<String> tagNames) {
        // null = 不动：局部更新（管理端那条全量入口）不带 tagNames 时不能把标签清光
        if (guideId == null || tagNames == null) {
            return;
        }
        // 整体替换：先清后插。比逐个 diff 简单，而且不会留下「标签已经从名单里去掉了、
        // 关联行还在」这种脏数据。
        travelGuideTagRefDao.delete(
                new QueryWrapper<TravelGuideTagRefEntity>().eq("guide_id", guideId));

        for (String name : normalizeTagNames(tagNames)) {
            travelGuideTagRefDao.insertIgnore(guideId, findOrCreateTagId(name));
        }
    }

    @Override
    public void removeByGuideIds(List<Long> guideIds) {
        if (guideIds == null || guideIds.isEmpty()) {
            return;
        }
        travelGuideTagRefDao.delete(
                new QueryWrapper<TravelGuideTagRefEntity>().in("guide_id", guideIds));
    }

    @Override
    public List<String> normalizeTagNames(List<String> raw) {
        if (raw == null) {
            return null;
        }
        // LinkedHashSet：既去重（同名只留一个）又保留作者点选的顺序
        Set<String> cleaned = new LinkedHashSet<>();
        for (String name : raw) {
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > MAX_TAG_NAME_LENGTH) {
                trimmed = trimmed.substring(0, MAX_TAG_NAME_LENGTH);
            }
            cleaned.add(trimmed);
        }
        return new ArrayList<>(cleaned);
    }

    /**
     * 标签名 → id，没有就建。名字上有唯一键，所以「插入后再查一次」就能拿到 id，
     * 不需要为了并发去加锁。
     */
    private Long findOrCreateTagId(String tagName) {
        Long id = travelGuideTagDao.selectIdByName(tagName);
        if (id != null) {
            return id;
        }
        travelGuideTagDao.insertIgnore(tagName);
        return travelGuideTagDao.selectIdByName(tagName);
    }
}
