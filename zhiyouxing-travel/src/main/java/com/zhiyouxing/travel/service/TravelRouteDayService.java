package com.zhiyouxing.travel.service;

import com.zhiyouxing.travel.entity.TravelRouteDayEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;

import java.util.List;

/**
 * 线路的每日行程。两个动作：读（回填到 View 上）与写（整条线路的行程整体替换）。
 *
 * <p>前端不需要感知这张表 —— 列表页拿到路线卡片时，daily 已经是排好序的数组，
 * 点开抽屉直接渲染，不用再为「每日安排」发一次请求。
 */
public interface TravelRouteDayService {

    /**
     * 批量回填每日行程。
     *
     * <p>本页所有线路的行程用**一次 IN 查询**取回再按 ref_id 归并，不写自定义 SQL、
     * 也不会 N+1。调用方式与 TravelGuideTagService.attachTagNames 一致，
     * 所以 /search 与 /detail 两处共用一个实现。
     *
     * <p>每条线路的 daily 都会被置成列表（没有行程就是空列表），不会给前端吐 null ——
     * 前端那句 `v-for="d in route.daily"` 才不用再套一层判空。
     */
    void attachDaily(List<TravelRouteView> routes);

    /**
     * 用给定的行程整体替换某条线路的行程（导游在管理端新建/编辑线路时调用）。
     *
     * <p>「整体替换」而不是逐条 diff：前端编辑器就是一个数组，增删改都是对数组操作，
     * 要算清哪一行是新增、哪一行被删、哪一行改了标题，比删干净重插复杂得多，
     * 而且顺序本来就是前端说了算的（day 序号由这里按数组顺序重编）。
     *
     * <p>只认 title 与 plan 两个字段：id / refId / day / addTime 一律由这里定，
     * 请求体里带什么都不作数 —— 免得前端改一行就把行程挂到别的线路上去。
     *
     * @param refId travel_route.id
     * @param daily 新行程；null 或空表示这条线路没有行程（旧行会被清掉）
     */
    void replaceDaily(Long refId, List<TravelRouteDayEntity> daily);
}
