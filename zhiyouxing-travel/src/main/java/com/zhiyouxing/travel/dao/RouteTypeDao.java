package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.RouteTypeEntity;
import com.zhiyouxing.travel.entity.view.RouteTypeView;
import com.zhiyouxing.travel.entity.vo.RouteTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 线路类型
 */
public interface RouteTypeDao extends BaseMapper<RouteTypeEntity> {
	
	List<RouteTypeVO> selectListVO(@Param("ew") Wrapper<RouteTypeEntity> wrapper);
	
	RouteTypeVO selectVO(@Param("ew") Wrapper<RouteTypeEntity> wrapper);
	
	List<RouteTypeView> selectListView(@Param("ew") Wrapper<RouteTypeEntity> wrapper);

	List<RouteTypeView> selectListView(Page<?> page,@Param("ew") Wrapper<RouteTypeEntity> wrapper);

	
	RouteTypeView selectView(@Param("ew") Wrapper<RouteTypeEntity> wrapper);
	

}
