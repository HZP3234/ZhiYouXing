package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.travel.dao.TravelRouteDayDao;
import com.zhiyouxing.travel.entity.TravelRouteDayEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;
import com.zhiyouxing.travel.service.TravelRouteDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线路每日行程。
 *
 * <p>与 TravelGuideTagServiceImpl.attachTagNames 同构：先给每个 View 挂上空列表
 * （宁可显示「还没录行程」也不要吐 null），空了就直接返回，
 * 否则一次 IN 把行程捞回来按 ref_id 归并。
 *
 * <p>注意 `in("ref_id", routeIds)` 前面必须判空：MyBatis-Plus 的 in() 不做空判，
 * 空集合会拼出 `ref_id IN ()`，MySQL 直接报语法错（是 500，不是「查不到」）。
 */
@Service("travelRouteDayService")
public class TravelRouteDayServiceImpl implements TravelRouteDayService {

    @Autowired
    private TravelRouteDayDao travelRouteDayDao;

    @Override
    public void attachDaily(List<TravelRouteView> routes) {
        if (routes == null || routes.isEmpty()) {
            return;
        }

        List<Long> routeIds = new ArrayList<>();
        for (TravelRouteView route : routes) {
            if (route == null) {
                continue;
            }
            route.setDaily(new ArrayList<>());
            if (route.getId() != null) {
                routeIds.add(route.getId());
            }
        }
        if (routeIds.isEmpty()) {
            return;
        }

        // day 升序在这里定死：抽屉按 Day 1 / Day 2 往下排，顺序不该由插入顺序决定
        List<TravelRouteDayEntity> rows = travelRouteDayDao.selectList(
                new QueryWrapper<TravelRouteDayEntity>()
                        .in("ref_id", routeIds)
                        .orderByAsc("day"));

        Map<Long, List<TravelRouteDayEntity>> byRoute = new HashMap<>();
        for (TravelRouteDayEntity row : rows) {
            if (row.getRefId() == null) {
                continue;
            }
            byRoute.computeIfAbsent(row.getRefId(), k -> new ArrayList<>()).add(row);
        }

        for (TravelRouteView route : routes) {
            if (route == null) {
                continue;
            }
            List<TravelRouteDayEntity> days = byRoute.get(route.getId());
            if (days != null) {
                route.setDaily(days);
            }
        }
    }

    @Override
    public void replaceDaily(Long refId, List<TravelRouteDayEntity> daily) {
        if (refId == null) {
            return;
        }
        travelRouteDayDao.delete(new QueryWrapper<TravelRouteDayEntity>().eq("ref_id", refId));
        if (daily == null || daily.isEmpty()) {
            return;
        }
        /*
         * 逐条 insert 而不是 saveBatch：一天一行、行程最多几十行，
         * 而 saveBatch 要走 ServiceImpl（这个类不是 IService，见类注释）。
         * day 按数组顺序重编，前端删掉中间一天后剩下的不会留空洞。
         */
        int day = 1;
        for (TravelRouteDayEntity item : daily) {
            TravelRouteDayEntity row = new TravelRouteDayEntity();
            row.setRefId(refId);
            row.setDay(day++);
            row.setTitle(item.getTitle());
            row.setPlan(item.getPlan());
            row.setAddTime(new Date());
            travelRouteDayDao.insert(row);
        }
    }
}
