package com.zhiyouxing.attraction.dao;

import com.zhiyouxing.attraction.entity.vo.RealNameInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读 user_identity（表归 user 服务，见其 IdentityController）。
 *
 * 用注解 SQL 而不是实体：attraction 服务里没有、也不该有这张表的增删改，
 * 需要的只是「这个账号认证到哪一步了，认证的是谁」这一个查询。
 *
 * 查不到（null）就表示没提交过实名认证 —— 这是下单的拒绝条件，不是异常；
 * 查到了还要看 audit_status，审核没通过一样拒绝（见 RealNameInfo.auditBlockReason）。
 */
public interface RealNameDao {

    @Select("SELECT real_name AS realName, id_card AS idCard, "
          + "audit_status AS auditStatus, audit_reply AS auditReply "
          + "FROM user_identity WHERE user_account = #{account} LIMIT 1")
    RealNameInfo selectByAccount(@Param("account") String account);
}
