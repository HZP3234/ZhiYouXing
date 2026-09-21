package com.zhiyouxing.hotel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.hotel.entity.HotelReservationEntity;
import com.zhiyouxing.hotel.entity.view.HotelReservationView;
import com.zhiyouxing.hotel.entity.vo.HotelReservationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 酒店预订
 */
public interface HotelReservationDao extends BaseMapper<HotelReservationEntity> {
	
	List<HotelReservationVO> selectListVO(@Param("ew") Wrapper<HotelReservationEntity> wrapper);
	
	HotelReservationVO selectVO(@Param("ew") Wrapper<HotelReservationEntity> wrapper);
	
	List<HotelReservationView> selectListView(@Param("ew") Wrapper<HotelReservationEntity> wrapper);

	List<HotelReservationView> selectListView(Page<?> page,@Param("ew") Wrapper<HotelReservationEntity> wrapper);

	
	HotelReservationView selectView(@Param("ew") Wrapper<HotelReservationEntity> wrapper);
	

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<HotelReservationEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<HotelReservationEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<HotelReservationEntity> wrapper);



}
