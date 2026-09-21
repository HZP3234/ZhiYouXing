package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.TravelGuideCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideCommentView;
import com.zhiyouxing.travel.entity.vo.TravelGuideCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 旅游攻略评论表
 */
public interface TravelGuideCommentService extends IService<TravelGuideCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TravelGuideCommentVO> selectListVO(Wrapper<TravelGuideCommentEntity> wrapper);
   	
   	TravelGuideCommentVO selectVO(@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);
   	
   	List<TravelGuideCommentView> selectListView(Wrapper<TravelGuideCommentEntity> wrapper);
   	
   	TravelGuideCommentView selectView(@Param("ew") Wrapper<TravelGuideCommentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TravelGuideCommentEntity> wrapper);
}

