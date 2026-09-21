package com.zhiyouxing.travel.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读 user 表里作者的昵称。
 *
 * <p>这张表属于 zhiyouxing-user 模块，但**不能**跨模块注入它的 Service：
 * 各模块的 @MapperScan 只扫自己的 dao 包，共享的只有 zhiyouxing-common。
 * 跨模块直读别人家的表在本仓库已有先例 —— zhiyouxing-attraction 与 zhiyouxing-hotel
 * 各有一份自己的 RealNameDao 裸 @Select 读 user_identity，这里照同一套做法。
 *
 * <p>只在写入攻略时调用一次（把昵称冗余落到 travel_guide.user_name），
 * 所以读列表/详情时零成本，也不会产生 N+1。
 */
public interface UserNameDao {

    /**
     * 按登录账号取昵称。取不到返回 null，调用方回落成账号本身。
     */
    @Select("SELECT user_name FROM `user` WHERE user_account = #{account} LIMIT 1")
    String selectUserNameByAccount(@Param("account") String account);
}
