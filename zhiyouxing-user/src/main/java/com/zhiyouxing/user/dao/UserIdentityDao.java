package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhiyouxing.user.entity.UserIdentityEntity;

/**
 * 实名认证。
 *
 * 和 ConsumptionDao 一样不走 Service 层：这张表只会被 IdentityController
 * 按账号查一行、插一行、改一行，套一层 IService 只是多两个转发文件。
 * 被 MapperScan 扫到即可（UserApplication 上扫的是 com.zhiyouxing.user.dao）。
 */
public interface UserIdentityDao extends BaseMapper<UserIdentityEntity> {
}
