package com.zhiyouxing.food.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.food.entity.RestaurantReservationEntity;
import com.zhiyouxing.food.entity.view.RestaurantReservationView;
import com.zhiyouxing.food.entity.vo.RestaurantReservationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 餐厅预约
 */
public interface RestaurantReservationDao extends BaseMapper<RestaurantReservationEntity> {
	
	List<RestaurantReservationVO> selectListVO(@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);
	
	RestaurantReservationVO selectVO(@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);
	
	List<RestaurantReservationView> selectListView(@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);

	List<RestaurantReservationView> selectListView(Page<?> page,@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);

	RestaurantReservationView selectView(@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);
}
