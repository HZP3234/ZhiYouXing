package com.zhiyouxing.hotel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.hotel.entity.HotelReservationEntity;
import com.zhiyouxing.hotel.entity.view.HotelReservationView;
import com.zhiyouxing.hotel.entity.vo.HotelReservationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 酒店预订
 */
public interface HotelReservationService extends IService<HotelReservationEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<HotelReservationVO> selectListVO(Wrapper<HotelReservationEntity> wrapper);
   	
   	HotelReservationVO selectVO(@Param("ew") Wrapper<HotelReservationEntity> wrapper);
   	
   	List<HotelReservationView> selectListView(Wrapper<HotelReservationEntity> wrapper);
   	
   	HotelReservationView selectView(@Param("ew") Wrapper<HotelReservationEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<HotelReservationEntity> wrapper);

   	

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<HotelReservationEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<HotelReservationEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<HotelReservationEntity> wrapper);

}

