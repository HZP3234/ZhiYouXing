package com.zhiyouxing.food.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.food.entity.RestaurantEntity;
import com.zhiyouxing.food.entity.view.RestaurantView;
import com.zhiyouxing.food.entity.vo.RestaurantVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 美食餐厅
 */
public interface RestaurantDao extends BaseMapper<RestaurantEntity> {
	
	List<RestaurantVO> selectListVO(@Param("ew") Wrapper<RestaurantEntity> wrapper);
	
	RestaurantVO selectVO(@Param("ew") Wrapper<RestaurantEntity> wrapper);
	
	List<RestaurantView> selectListView(@Param("ew") Wrapper<RestaurantEntity> wrapper);

	List<RestaurantView> selectListView(Page<?> page,@Param("ew") Wrapper<RestaurantEntity> wrapper);

	RestaurantView selectView(@Param("ew") Wrapper<RestaurantEntity> wrapper);
}
