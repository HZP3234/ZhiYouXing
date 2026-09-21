package com.zhiyouxing.travel.dao;

import com.zhiyouxing.travel.entity.vo.RealNameInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 只读 user_identity（表归 user 服务，见其 IdentityController）。
 *
 * 用注解 SQL 而不是实体：travel 服务里没有、也不该有这张表的增删改，
 * 需要的只是「这个账号认证到哪一步了，认证的是谁」这一个查询。
 *
 * 查不到（null）就表示没提交过实名认证 —— 这是报团下单的拒绝条件，不是异常；
 * 查到了还要看 audit_status，审核没通过一样拒绝（见 RealNameInfo.auditBlockReason）。
 *
 * 与 com.zhiyouxing.attraction.dao.RealNameDao、com.zhiyouxing.hotel.dao.RealNameDao
 * 同源，是同一段 SQL 的第三份拷贝。第三个模块出现后本来到了「提到 zhiyouxing-common」
 * 的时机，仍然选择再抄一份：改 common 必须重新 install，否则脚本起服务会读到 .m2
 * 里的旧 jar 报 NoClassDefFoundError，而这段 SQL 只有一行、三份也还看得过来。
 * 第四处再要读这张表时，就该往上提了。
 */
public interface RealNameDao {

    @Select("SELECT real_name AS realName, id_card AS idCard, "
          + "audit_status AS auditStatus, audit_reply AS auditReply "
          + "FROM user_identity WHERE user_account = #{account} LIMIT 1")
    RealNameInfo selectByAccount(@Param("account") String account);
}
