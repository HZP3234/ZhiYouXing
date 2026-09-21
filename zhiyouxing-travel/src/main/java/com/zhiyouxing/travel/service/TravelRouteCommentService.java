package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.TravelRouteCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteCommentView;
import com.zhiyouxing.travel.entity.vo.TravelRouteCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 旅游线路评论表
 */
public interface TravelRouteCommentService extends IService<TravelRouteCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TravelRouteCommentVO> selectListVO(Wrapper<TravelRouteCommentEntity> wrapper);
   	
   	TravelRouteCommentVO selectVO(@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);
   	
   	List<TravelRouteCommentView> selectListView(Wrapper<TravelRouteCommentEntity> wrapper);
   	
   	TravelRouteCommentView selectView(@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TravelRouteCommentEntity> wrapper);

}

