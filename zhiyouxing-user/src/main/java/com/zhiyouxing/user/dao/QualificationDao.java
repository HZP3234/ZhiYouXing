package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyouxing.user.entity.QualificationEntity;

/**
 * 管理端资质。
 *
 * 与 UserIdentityDao 同样不走 Service 层：一个账号只查一行、写一行，
 * 管理端多一个分页与一次按 id 改状态。被 MapperScan 扫到即可。
 */
public interface QualificationDao extends BaseMapper<QualificationEntity> {
}
