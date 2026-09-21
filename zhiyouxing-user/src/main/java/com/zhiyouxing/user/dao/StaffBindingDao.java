package com.zhiyouxing.user.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 资质审核通过后，把「所属单位」回写成该角色在自己表里的归属列。
 *
 * <p>三个前台角色的业务接口全部按各自表里的归属列划作用域
 * （attraction_staff.attraction_name / hotel_staff.hotel_name /
 * restaurant_staff.restaurant_name，见各服务的 *StaffScopeController.scopeName）。
 * 新注册的账号这一列是 NULL，不回写的话该前台一创建内容就被
 * 「当前账号未绑定景点 / 酒店 / 餐厅」拦下。
 *
 * <p>为什么不复用各 *StaffScopeController：那些接口在 attraction / hotel / food 服务里，
 * 而审核动作发生在 user 服务的 /users/** 下（管理端请求前缀固定是管理员自己的表名）。
 * user 服务连的是同一个 zhiyouxing 库，跨服务直接改这几张表已有先例
 * （AttractionAuditDao / HotelInfoAuditDao / RestaurantAuditDao 是同一套写法）。
 *
 * <p>三条语句各写各自的表名与列名，不用 ${} 拼标识符 —— 表名来自
 * qualification.table_name，虽经 QualificationController.SUBMITTERS 白名单校验过，
 * 也不该把标识符塞进 SQL 模板。
 */
public interface StaffBindingDao {

    @Update("UPDATE attraction_staff SET attraction_name = #{value} WHERE staff_account = #{account}")
    int bindAttraction(@Param("account") String account, @Param("value") String value);

    @Update("UPDATE hotel_staff SET hotel_name = #{value} WHERE staff_account = #{account}")
    int bindHotel(@Param("account") String account, @Param("value") String value);

    @Update("UPDATE restaurant_staff SET restaurant_name = #{value} WHERE staff_account = #{account}")
    int bindRestaurant(@Param("account") String account, @Param("value") String value);
}
