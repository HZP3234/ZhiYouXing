package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.TourGuideEntity;
import com.zhiyouxing.travel.entity.view.TourGuideView;
import com.zhiyouxing.travel.entity.vo.TourGuideVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 导游
 */
public interface TourGuideService extends IService<TourGuideEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TourGuideVO> selectListVO(Wrapper<TourGuideEntity> wrapper);
   	
   	TourGuideVO selectVO(@Param("ew") Wrapper<TourGuideEntity> wrapper);
   	
   	List<TourGuideView> selectListView(Wrapper<TourGuideEntity> wrapper);
   	
   	TourGuideView selectView(@Param("ew") Wrapper<TourGuideEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TourGuideEntity> wrapper);

}

