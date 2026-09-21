package com.zhiyouxing.attraction.dao;

import com.zhiyouxing.attraction.entity.AttractionCommentEntity;
import com.zhiyouxing.attraction.entity.view.AttractionCommentView;
import com.zhiyouxing.attraction.entity.vo.AttractionCommentVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 热门景点评论表
 */
public interface AttractionCommentDao extends BaseMapper<AttractionCommentEntity> {
	
	List<AttractionCommentVO> selectListVO(@Param("ew") Wrapper<AttractionCommentEntity> wrapper);
	
	AttractionCommentVO selectVO(@Param("ew") Wrapper<AttractionCommentEntity> wrapper);
	
	List<AttractionCommentView> selectListView(@Param("ew") Wrapper<AttractionCommentEntity> wrapper);

	List<AttractionCommentView> selectListView(Page<?> page,@Param("ew") Wrapper<AttractionCommentEntity> wrapper);

	
	AttractionCommentView selectView(@Param("ew") Wrapper<AttractionCommentEntity> wrapper);
	

}
