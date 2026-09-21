package com.zhiyouxing.food.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.food.entity.RestaurantEntity;
import com.zhiyouxing.food.entity.view.RestaurantView;
import com.zhiyouxing.food.entity.vo.RestaurantVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 美食餐厅
 */
public interface RestaurantService extends IService<RestaurantEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<RestaurantVO> selectListVO(Wrapper<RestaurantEntity> wrapper);
   	
   	RestaurantVO selectVO(@Param("ew") Wrapper<RestaurantEntity> wrapper);
   	
   	List<RestaurantView> selectListView(Wrapper<RestaurantEntity> wrapper);
   	
   	RestaurantView selectView(@Param("ew") Wrapper<RestaurantEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<RestaurantEntity> wrapper);

   	

}

