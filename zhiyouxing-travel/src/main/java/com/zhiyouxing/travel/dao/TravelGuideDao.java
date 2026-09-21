package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.TravelGuideEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideView;
import com.zhiyouxing.travel.entity.vo.TravelGuideVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 旅游攻略
 */
public interface TravelGuideDao extends BaseMapper<TravelGuideEntity> {
	
	List<TravelGuideVO> selectListVO(@Param("ew") Wrapper<TravelGuideEntity> wrapper);
	
	TravelGuideVO selectVO(@Param("ew") Wrapper<TravelGuideEntity> wrapper);
	
	List<TravelGuideView> selectListView(@Param("ew") Wrapper<TravelGuideEntity> wrapper);

	List<TravelGuideView> selectListView(Page<?> page,@Param("ew") Wrapper<TravelGuideEntity> wrapper);

	
	TravelGuideView selectView(@Param("ew") Wrapper<TravelGuideEntity> wrapper);
	

}
