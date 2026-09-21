package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.TravelGuideCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideCommentView;
import com.zhiyouxing.travel.entity.vo.TravelGuideCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 旅游攻略评论表

 */
public interface TravelGuideCommentDao extends BaseMapper<TravelGuideCommentEntity> {
	
	List<TravelGuideCommentVO> selectListVO(@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);
	
	TravelGuideCommentVO selectVO(@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);
	
	List<TravelGuideCommentView> selectListView(@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);

	List<TravelGuideCommentView> selectListView(Page<?> page,@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);

	
	TravelGuideCommentView selectView(@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);
	

}
