package com.zhiyouxing.attraction.service;

import com.zhiyouxing.attraction.entity.AttractionCommentEntity;
import com.zhiyouxing.attraction.entity.view.AttractionCommentView;
import com.zhiyouxing.attraction.entity.vo.AttractionCommentVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 热门景点评论表
 */
public interface AttractionCommentService extends IService<AttractionCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<AttractionCommentVO> selectListVO(Wrapper<AttractionCommentEntity> wrapper);
   	
   	AttractionCommentVO selectVO(@Param("ew") Wrapper<AttractionCommentEntity> wrapper);
   	
   	List<AttractionCommentView> selectListView(Wrapper<AttractionCommentEntity> wrapper);
   	
   	AttractionCommentView selectView(@Param("ew") Wrapper<AttractionCommentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<AttractionCommentEntity> wrapper);

   	

}

