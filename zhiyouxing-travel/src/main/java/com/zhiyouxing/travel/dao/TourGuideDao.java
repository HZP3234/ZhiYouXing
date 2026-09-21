package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.TourGuideEntity;
import com.zhiyouxing.travel.entity.view.TourGuideView;
import com.zhiyouxing.travel.entity.vo.TourGuideVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 导游
 */
public interface TourGuideDao extends BaseMapper<TourGuideEntity> {
	
	List<TourGuideVO> selectListVO(@Param("ew") Wrapper<TourGuideEntity> wrapper);
	
	TourGuideVO selectVO(@Param("ew") Wrapper<TourGuideEntity> wrapper);
	
	List<TourGuideView> selectListView(@Param("ew") Wrapper<TourGuideEntity> wrapper);

	List<TourGuideView> selectListView(Page<?> page,@Param("ew") Wrapper<TourGuideEntity> wrapper);

	TourGuideView selectView(@Param("ew") Wrapper<TourGuideEntity> wrapper);

}
