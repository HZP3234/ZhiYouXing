package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.TravelGuideEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideView;
import com.zhiyouxing.travel.entity.vo.TravelGuideVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 旅游攻略
 */
public interface TravelGuideService extends IService<TravelGuideEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TravelGuideVO> selectListVO(Wrapper<TravelGuideEntity> wrapper);
   	
   	TravelGuideVO selectVO(@Param("ew") Wrapper<TravelGuideEntity> wrapper);
   	
   	List<TravelGuideView> selectListView(Wrapper<TravelGuideEntity> wrapper);
   	
   	TravelGuideView selectView(@Param("ew") Wrapper<TravelGuideEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TravelGuideEntity> wrapper);

}

