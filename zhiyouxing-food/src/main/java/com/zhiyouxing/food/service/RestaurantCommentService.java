package com.zhiyouxing.food.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.food.entity.RestaurantCommentEntity;
import com.zhiyouxing.food.entity.view.RestaurantCommentView;
import com.zhiyouxing.food.entity.vo.RestaurantCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 美食餐厅评论表
 */
public interface RestaurantCommentService extends IService<RestaurantCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<RestaurantCommentVO> selectListVO(Wrapper<RestaurantCommentEntity> wrapper);
   	
   	RestaurantCommentVO selectVO(@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);
   	
   	List<RestaurantCommentView> selectListView(Wrapper<RestaurantCommentEntity> wrapper);
   	
   	RestaurantCommentView selectView(@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<RestaurantCommentEntity> wrapper);

   	

}

