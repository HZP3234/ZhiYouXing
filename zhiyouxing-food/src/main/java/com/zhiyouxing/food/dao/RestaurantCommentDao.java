package com.zhiyouxing.food.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.food.entity.RestaurantCommentEntity;
import com.zhiyouxing.food.entity.view.RestaurantCommentView;
import com.zhiyouxing.food.entity.vo.RestaurantCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 美食餐厅评论表
 */
public interface RestaurantCommentDao extends BaseMapper<RestaurantCommentEntity> {
	
	List<RestaurantCommentVO> selectListVO(@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);
	
	RestaurantCommentVO selectVO(@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);
	
	List<RestaurantCommentView> selectListView(@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);

	List<RestaurantCommentView> selectListView(Page<?> page,@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);
	
	RestaurantCommentView selectView(@Param("ew") Wrapper<RestaurantCommentEntity> wrapper);

}
