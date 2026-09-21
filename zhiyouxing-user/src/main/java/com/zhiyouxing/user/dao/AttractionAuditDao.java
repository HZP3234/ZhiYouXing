package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyouxing.user.entity.AttractionAuditEntity;

/**
 * 景点审核台。
 *
 * 与 HotelInfoAuditDao / TravelRouteAuditDao 同样不走 Service 层：管理端只做
 * 「分页查 + 按 id 改审核结论」，被 MapperScan 扫到即可。
 */
public interface AttractionAuditDao extends BaseMapper<AttractionAuditEntity> {
}
