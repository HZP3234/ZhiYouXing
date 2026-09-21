package com.zhiyouxing.user.dao;

import com.zhiyouxing.user.entity.vo.ConsumptionVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 个人中心的消费记录。
 *
 * 用注解 SQL 而不是 MyBatis-Plus 实体：四张订单表分别属于 attraction / hotel / travel / food
 * 四个服务，user 服务里本来没有它们的实体。个人中心要的只是一张统一的卡片，
 * 为此在本模块复制四套 Entity / Service / XML 并不划算，四条 SELECT 加几条定向
 * UPDATE 更直接。
 *
 * user 服务与那四个服务连的是同一个库（zhiyouxing），所以跨表读写是成立的。
 * 代价是这里的 SQL 认死了对方的表结构 —— 那四张表改列名时要一起改。
 *
 * 所有语句都带 user_account = #{account}，账号由 Controller 从登录态取。
 * 「这行是不是本人的」不做成先查后判的两步：归属条件直接写进 WHERE，
 * 少一处漏判的机会，也少一次往返。
 */
public interface ConsumptionDao {

	/* ------------------------------------------------------------------
	 * 列表
	 *
	 * 每条 SQL 把「来源 / 来源名 / 标题 / 数量文案」就地拼好，
	 * 让四张表的结果能塞进同一个 ConsumptionVO。
	 * LIMIT 100 是给「最近消费记录」兜底：这是个只读摘要，不需要全量。
	 * ------------------------------------------------------------------ */

	@Select("SELECT 'ticket' AS source, '景点门票' AS sourceLabel, id, order_no AS orderNo, "
	      + "attraction_name AS title, image, total_amount AS amount, "
	      + "CONCAT('×', COALESCE(quantity, 1), ' 张') AS quantityDesc, "
	      + "add_time AS addTime, is_pay AS isPay, is_comment AS isComment "
	      + "FROM ticket_order WHERE user_account = #{account} "
	      + "ORDER BY add_time DESC LIMIT 100")
	List<ConsumptionVO> selectTicketOrders(@Param("account") String account);

	/* hotel_name 可能为空，CONCAT_WS 会跳过 NULL，正好拼成「酒店 · 房型」 */
	@Select("SELECT 'hotel' AS source, '酒店预订' AS sourceLabel, id, reservation_no AS orderNo, "
	      + "CONCAT_WS(' · ', hotel_name, room_name) AS title, room_image AS image, total_amount AS amount, "
	      + "CONCAT(COALESCE(room_count, 1), ' 间 · ', COALESCE(stay_days, 1), ' 晚') AS quantityDesc, "
	      + "add_time AS addTime, is_pay AS isPay, is_comment AS isComment "
	      + "FROM hotel_reservation WHERE user_account = #{account} "
	      + "ORDER BY add_time DESC LIMIT 100")
	List<ConsumptionVO> selectHotelReservations(@Param("account") String account);

	/* 报团表没有订单号列，orderNo 留 null 由前端显示成「—」 */
	@Select("SELECT 'group' AS source, '旅游报团' AS sourceLabel, id, route_name AS title, "
	      + "route_image AS image, group_tour_amount AS amount, "
	      + "CONCAT(COALESCE(signup_count, 1), ' 人') AS quantityDesc, "
	      + "add_time AS addTime, is_pay AS isPay, is_comment AS isComment "
	      + "FROM group_tour WHERE user_account = #{account} "
	      + "ORDER BY add_time DESC LIMIT 100")
	List<ConsumptionVO> selectGroupTours(@Param("account") String account);

	/*
	 * 餐厅预约。四类里唯一没有支付语义的一张表：没有 is_pay / is_comment / 金额，
	 * 状态只有 audit_status（待审核 / 已通过 / 已驳回）。所以 amount、isPay、
	 * isComment 三项直接给 NULL —— 前端拿到 null 就知道这行不参与支付与评价，
	 * 不必再去比对来源字符串。
	 *
	 * reservation_no 由下单时前端生成（genOrderNo('CY')）并落库，这里照常回传。
	 * 改成这列之前下的单会是 null，前端显示成「—」。
	 *
	 * reservation_time / dining_remark 是「预约记录」区别于普通订单的两列：
	 * 别的三类订单 addTime 就是有意义的时间（下单那刻），而预约的重点是
	 * 「约的是哪天几点的座位」，所以这两列必须一起带出去；不然卡片上
	 * 只有一个下单位置，用户看不出自己约的是什么时段。
	 */
	@Select("SELECT 'restaurant' AS source, '餐厅预约' AS sourceLabel, id, "
	      + "reservation_no AS orderNo, "
	      + "restaurant_name AS title, restaurant_image AS image, NULL AS amount, "
	      + "CONCAT(COALESCE(diner_count, 1), ' 位') AS quantityDesc, "
	      + "add_time AS addTime, NULL AS isPay, NULL AS isComment, "
	      + "reservation_time AS reservationTime, dining_remark AS remark, "
	      + "audit_status AS auditStatus, audit_reply AS auditReply "
	      + "FROM restaurant_reservation WHERE user_account = #{account} "
	      + "ORDER BY add_time DESC LIMIT 100")
	List<ConsumptionVO> selectRestaurantReservations(@Param("account") String account);

	/* ------------------------------------------------------------------
	 * 超时取消
	 *
	 * 「未支付超过 N 分钟」的单子就地翻成「已取消」。三条语句只在表名上不同，
	 * 判据完全一致：本人的、还没动过状态位的、且 add_time 已经过期。
	 *
	 * 条件里坚持写 user_account，看起来多余（下面 cancel 那组才是按人点的），
	 * 但这里是列表接口每次进页面都会整表扫一遍的语句，少一个限定条件就等于
	 * 把别人没付的单也一起取消了 —— 代价太大，不如每次都带上。
	 *
	 * 只认「未支付 / NULL」两个态：已经付过或已经取消的单，不管放多久都不该被改写。
	 * ------------------------------------------------------------------ */

	@Update("UPDATE ticket_order SET is_pay = '已取消' "
	      + "WHERE user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付') "
	      + "AND add_time IS NOT NULL "
	      + "AND add_time < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
	int expireTicketOrders(@Param("account") String account, @Param("minutes") int minutes);

	@Update("UPDATE hotel_reservation SET is_pay = '已取消' "
	      + "WHERE user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付') "
	      + "AND add_time IS NOT NULL "
	      + "AND add_time < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
	int expireHotelReservations(@Param("account") String account, @Param("minutes") int minutes);

	@Update("UPDATE group_tour SET is_pay = '已取消' "
	      + "WHERE user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付') "
	      + "AND add_time IS NOT NULL "
	      + "AND add_time < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
	int expireGroupTours(@Param("account") String account, @Param("minutes") int minutes);

	/* ------------------------------------------------------------------
	 * 去支付
	 *
	 * 三条语句结构一致，各自改自己那张表。返回影响行数，
	 * 0 表示这行不存在、不属于当前账号，或者状态已经不是「未支付」了。
	 *
	 * 后一个条件（is_pay 必须是未支付）是取消功能带来的：没有它，
	 * 「超时已取消」的单子照样能被付掉 —— 状态位翻回「已支付」，
	 * 用户看到的却是一张已经作废的订单。也顺带堵住了重复支付。
	 * ------------------------------------------------------------------ */

	@Update("UPDATE ticket_order SET is_pay = '已支付' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付')")
	int payTicketOrder(@Param("id") Long id, @Param("account") String account);

	@Update("UPDATE hotel_reservation SET is_pay = '已支付' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付')")
	int payHotelReservation(@Param("id") Long id, @Param("account") String account);

	@Update("UPDATE group_tour SET is_pay = '已支付' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付')")
	int payGroupTour(@Param("id") Long id, @Param("account") String account);

	/* ------------------------------------------------------------------
	 * 手动取消
	 *
	 * 和超时取消同一套判据，只是把「过了多久」换成「点的是哪一行」，
	 * 所以两份 SQL 长得很像 —— 刻意不合并成一条：超时那条要按时间扫，
	 * 这条按主键点，用同一句拼出来的话，两个场景都得写一堆可选条件。
	 * ------------------------------------------------------------------ */

	@Update("UPDATE ticket_order SET is_pay = '已取消' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付')")
	int cancelTicketOrder(@Param("id") Long id, @Param("account") String account);

	@Update("UPDATE hotel_reservation SET is_pay = '已取消' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付')")
	int cancelHotelReservation(@Param("id") Long id, @Param("account") String account);

	@Update("UPDATE group_tour SET is_pay = '已取消' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_pay IS NULL OR is_pay = '未支付')")
	int cancelGroupTour(@Param("id") Long id, @Param("account") String account);

	/* ------------------------------------------------------------------
	 * 去评价
	 *
	 * 评价要写的是详情页在用的那几张评论表，而评论表的 ref_id 指向主表 id
	 * （景点 / 房型 / 线路），订单表里存的却只有名称 —— 订单表没有主表 id 列。
	 * 所以先用名称把 ref_id 反查出来：下面三条 JOIN 同时完成了
	 * 「这行是本人的」和「名称对得上主表」两件事，查不到就返回 null。
	 *
	 * 房型名称在不同酒店之间会重名（三个酒店都有「高级大床房」），
	 * 所以 hotel 这条必须连 hotel_name 一起对，否则会把评价挂到别家酒店上。
	 *
	 * 评论表的 user_id 是数字 id，和订单表里的 user_account 串不是一回事，
	 * 由 Controller 从登录态取 user_id 传进来。
	 * ------------------------------------------------------------------ */

	@Select("SELECT a.id FROM attraction a, ticket_order o "
	      + "WHERE o.id = #{id} AND o.user_account = #{account} "
	      + "AND a.attraction_name = o.attraction_name LIMIT 1")
	Long selectTicketRefId(@Param("id") Long id, @Param("account") String account);

	@Select("SELECT h.id FROM hotel_info h, hotel_reservation r "
	      + "WHERE r.id = #{id} AND r.user_account = #{account} "
	      + "AND h.room_name = r.room_name AND h.hotel_name = r.hotel_name LIMIT 1")
	Long selectHotelRefId(@Param("id") Long id, @Param("account") String account);

	@Select("SELECT t.id FROM travel_route t, group_tour g "
	      + "WHERE g.id = #{id} AND g.user_account = #{account} "
	      + "AND t.route_name = g.route_name LIMIT 1")
	Long selectGroupRefId(@Param("id") Long id, @Param("account") String account);

	/*
	 * 标记已评价。条件里带 is_comment <> '已评价'，是为了让这条 UPDATE
	 * 同时当防重复的闸门：并发点两次「提交评价」时，只有一次能改到行，
	 * 另一次影响 0 行，Controller 直接回「已经评价过了」，不会再插一条评论。
	 */
	@Update("UPDATE ticket_order SET is_comment = '已评价' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_comment IS NULL OR is_comment <> '已评价')")
	int markTicketOrderCommented(@Param("id") Long id, @Param("account") String account);

	@Update("UPDATE hotel_reservation SET is_comment = '已评价' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_comment IS NULL OR is_comment <> '已评价')")
	int markHotelReservationCommented(@Param("id") Long id, @Param("account") String account);

	@Update("UPDATE group_tour SET is_comment = '已评价' "
	      + "WHERE id = #{id} AND user_account = #{account} "
	      + "AND (is_comment IS NULL OR is_comment <> '已评价')")
	int markGroupTourCommented(@Param("id") Long id, @Param("account") String account);

	/*
	 * 写入评论表。add_time 有 DEFAULT CURRENT_TIMESTAMP，不用显式给。
	 * travel_route_comment 是唯一没有 score 列的评论表（见 zhiyouxing.sql），
	 * 所以报团的评价只有文字。
	 */
	@Insert("INSERT INTO attraction_comment (ref_id, user_id, avatar_url, nickname, content, score) "
	      + "VALUES (#{refId}, #{userId}, #{avatar}, #{nickname}, #{content}, #{score})")
	int insertAttractionComment(@Param("refId") Long refId, @Param("userId") Long userId,
	                            @Param("avatar") String avatar, @Param("nickname") String nickname,
	                            @Param("content") String content, @Param("score") Double score);

	@Insert("INSERT INTO hotel_comment (ref_id, user_id, avatar_url, nickname, content, score) "
	      + "VALUES (#{refId}, #{userId}, #{avatar}, #{nickname}, #{content}, #{score})")
	int insertHotelComment(@Param("refId") Long refId, @Param("userId") Long userId,
	                       @Param("avatar") String avatar, @Param("nickname") String nickname,
	                       @Param("content") String content, @Param("score") Double score);

	@Insert("INSERT INTO travel_route_comment (ref_id, user_id, avatar_url, nickname, content) "
	      + "VALUES (#{refId}, #{userId}, #{avatar}, #{nickname}, #{content})")
	int insertRouteComment(@Param("refId") Long refId, @Param("userId") Long userId,
	                       @Param("avatar") String avatar, @Param("nickname") String nickname,
	                       @Param("content") String content);
}
