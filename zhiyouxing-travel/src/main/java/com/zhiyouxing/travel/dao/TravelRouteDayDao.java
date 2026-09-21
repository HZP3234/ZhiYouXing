package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyouxing.travel.entity.TravelRouteDayEntity;

/**
 * 线路每日行程表。
 *
 * <p>和 TravelGuideTagRefDao 一样：读写一律用 BaseMapper 自带的
 * selectList / delete / insert 配 QueryWrapper（见 TravelRouteDayServiceImpl），
 * 所以这里一条自定义 SQL 都没有。
 *
 * <p>写入口只有一个：导游在管理端保存线路时，TravelRouteDayService.replaceDaily
 * 把这条线路的行程删干净再按数组顺序重插（前端有个逐日编辑器）。
 * 这张表仍然没有自己的控制器，不对外暴露增删改。
 */
public interface TravelRouteDayDao extends BaseMapper<TravelRouteDayEntity> {
}
