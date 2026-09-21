package com.zhiyouxing.food.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.food.entity.RestaurantReservationEntity;
import com.zhiyouxing.food.entity.view.RestaurantReservationView;
import com.zhiyouxing.food.entity.vo.RestaurantReservationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 餐厅预约
 */
public interface RestaurantReservationService extends IService<RestaurantReservationEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<RestaurantReservationVO> selectListVO(Wrapper<RestaurantReservationEntity> wrapper);
   	
   	RestaurantReservationVO selectVO(@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);
   	
   	List<RestaurantReservationView> selectListView(Wrapper<RestaurantReservationEntity> wrapper);
   	
   	RestaurantReservationView selectView(@Param("ew") Wrapper<RestaurantReservationEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<RestaurantReservationEntity> wrapper);

   	

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<RestaurantReservationEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<RestaurantReservationEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<RestaurantReservationEntity> wrapper);



}

