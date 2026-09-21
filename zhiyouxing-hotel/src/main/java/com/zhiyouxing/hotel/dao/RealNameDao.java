package com.zhiyouxing.hotel.dao;

import com.zhiyouxing.hotel.entity.vo.RealNameInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读 user_identity（表归 user 服务，见其 IdentityController）。
 *
 * 用注解 SQL 而不是实体：hotel 服务里没有、也不该有这张表的增删改，
 * 需要的只是「这个账号认证到哪一步了，认证的是谁」这一个查询。
 *
 * 查不到（null）就表示没提交过实名认证 —— 这是下单的拒绝条件，不是异常；
 * 查到了还要看 audit_status，审核没通过一样拒绝（见 RealNameInfo.auditBlockReason）。
 *
 * 与 com.zhiyouxing.attraction.dao.RealNameDao 同源，是同一段 SQL 的第二份拷贝：
 * 门票下单也要把这道关，但两个服务的包名不同（各自的 @MapperScan 只扫自己的包），
 * 而提到 zhiyouxing-common 会牵动「改 common 必须重新 install，否则脚本起服务
 * 读到 .m2 旧 jar 报 NoClassDefFoundError」那条链路。若将来第三个模块也要读
 * 这张表，那时再往上提才划算。第三个模块（travel 报团下单）出现后仍是照抄的一份，
 * 为什么不借那次机会往上提，见 com.zhiyouxing.travel.dao.RealNameDao 的类注释。
 */
public interface RealNameDao {

    @Select("SELECT real_name AS realName, id_card AS idCard, "
          + "audit_status AS auditStatus, audit_reply AS auditReply "
          + "FROM user_identity WHERE user_account = #{account} LIMIT 1")
    RealNameInfo selectByAccount(@Param("account") String account);
}
