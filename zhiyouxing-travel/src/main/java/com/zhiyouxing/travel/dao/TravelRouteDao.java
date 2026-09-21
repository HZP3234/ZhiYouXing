package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;
import com.zhiyouxing.travel.entity.vo.TravelRouteVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 旅游线路
 */
public interface TravelRouteDao extends BaseMapper<TravelRouteEntity> {
	
	List<TravelRouteVO> selectListVO(@Param("ew") Wrapper<TravelRouteEntity> wrapper);
	
	TravelRouteVO selectVO(@Param("ew") Wrapper<TravelRouteEntity> wrapper);
	
	List<TravelRouteView> selectListView(@Param("ew") Wrapper<TravelRouteEntity> wrapper);

	List<TravelRouteView> selectListView(Page<?> page,@Param("ew") Wrapper<TravelRouteEntity> wrapper);

	
	TravelRouteView selectView(@Param("ew") Wrapper<TravelRouteEntity> wrapper);
	

}
