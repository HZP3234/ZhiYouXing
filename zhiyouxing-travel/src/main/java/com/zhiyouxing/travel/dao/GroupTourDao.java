package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.GroupTourEntity;
import com.zhiyouxing.travel.entity.view.GroupTourView;
import com.zhiyouxing.travel.entity.vo.GroupTourVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 报团信息
 */
public interface GroupTourDao extends BaseMapper<GroupTourEntity> {

	/**
	 * 这条线路上已被占用的名额。
	 *
	 * 按 route_name 关联而不是线路 id：group_tour 没有 route_id 列，
	 * 订单与线路的关系全项目都靠名称对（同 ConsumptionDao.selectGroupRefId）。
	 *
	 * 两种单算占用：已支付的，以及还在支付时限内的未支付单 ——
	 * 未付的单也要占着名额，否则同一批人反复下单就能把名额超卖。
	 * 超时的未支付单不算：「已取消」的单要排除，
	 * 而超时取消在 user 服务那边是惰性的（挂在 /user/consumption/list 上），
	 * 光靠 is_pay 判断的话，一笔下了不付款、用户又再没打开个人中心的单
	 * 会永久压着名额，把整条线路锁死。
	 */
	@Select("SELECT COALESCE(SUM(signup_count), 0) FROM group_tour "
	      + "WHERE route_name = #{routeName} "
	      + "AND (is_pay = '已支付' "
	      + "     OR ((is_pay IS NULL OR is_pay = '未支付') "
	      + "         AND add_time IS NOT NULL "
	      + "         AND add_time > DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)))")
	Integer sumReservedSeats(@Param("routeName") String routeName, @Param("minutes") int minutes);

	List<GroupTourVO> selectListVO(@Param("ew") Wrapper<GroupTourEntity> wrapper);
	
	GroupTourVO selectVO(@Param("ew") Wrapper<GroupTourEntity> wrapper);
	
	List<GroupTourView> selectListView(@Param("ew") Wrapper<GroupTourEntity> wrapper);

	List<GroupTourView> selectListView(Page<?> page,@Param("ew") Wrapper<GroupTourEntity> wrapper);

	
	GroupTourView selectView(@Param("ew") Wrapper<GroupTourEntity> wrapper);
	

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<GroupTourEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<GroupTourEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<GroupTourEntity> wrapper);



}
