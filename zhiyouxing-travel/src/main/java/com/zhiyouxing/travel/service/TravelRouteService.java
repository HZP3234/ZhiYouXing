package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;
import com.zhiyouxing.travel.entity.vo.TravelRouteVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 旅游线路
 */
public interface TravelRouteService extends IService<TravelRouteEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TravelRouteVO> selectListVO(Wrapper<TravelRouteEntity> wrapper);
   	
   	TravelRouteVO selectVO(@Param("ew") Wrapper<TravelRouteEntity> wrapper);
   	
   	List<TravelRouteView> selectListView(Wrapper<TravelRouteEntity> wrapper);
   	
   	TravelRouteView selectView(@Param("ew") Wrapper<TravelRouteEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TravelRouteEntity> wrapper);

}

