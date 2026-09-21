package com.zhiyouxing.attraction.dao;

import com.zhiyouxing.attraction.entity.AttractionEntity;
import com.zhiyouxing.attraction.entity.view.AttractionView;
import com.zhiyouxing.attraction.entity.vo.AttractionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 热门景点
 */
public interface AttractionDao extends BaseMapper<AttractionEntity> {
	
	List<AttractionVO> selectListVO(@Param("ew") Wrapper<AttractionEntity> wrapper);
	
	AttractionVO selectVO(@Param("ew") Wrapper<AttractionEntity> wrapper);
	
	List<AttractionView> selectListView(@Param("ew") Wrapper<AttractionEntity> wrapper);

	List<AttractionView> selectListView(Page<?> page,@Param("ew") Wrapper<AttractionEntity> wrapper);

	
	AttractionView selectView(@Param("ew") Wrapper<AttractionEntity> wrapper);
	

}
