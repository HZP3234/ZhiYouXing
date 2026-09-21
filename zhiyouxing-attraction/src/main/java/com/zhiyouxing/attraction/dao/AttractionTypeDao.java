package com.zhiyouxing.attraction.dao;

import com.zhiyouxing.attraction.entity.AttractionTypeEntity;
import com.zhiyouxing.attraction.entity.view.AttractionTypeView;
import com.zhiyouxing.attraction.entity.vo.AttractionTypeVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 景点类型
 */
public interface AttractionTypeDao extends BaseMapper<AttractionTypeEntity> {
	
	List<AttractionTypeVO> selectListVO(@Param("ew") Wrapper<AttractionTypeEntity> wrapper);
	
	AttractionTypeVO selectVO(@Param("ew") Wrapper<AttractionTypeEntity> wrapper);
	
	List<AttractionTypeView> selectListView(@Param("ew") Wrapper<AttractionTypeEntity> wrapper);

	List<AttractionTypeView> selectListView(Page<?> page,@Param("ew") Wrapper<AttractionTypeEntity> wrapper);

	
	AttractionTypeView selectView(@Param("ew") Wrapper<AttractionTypeEntity> wrapper);
	

}
