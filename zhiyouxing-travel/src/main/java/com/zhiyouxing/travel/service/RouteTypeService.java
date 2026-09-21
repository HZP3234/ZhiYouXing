package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.RouteTypeEntity;
import com.zhiyouxing.travel.entity.view.RouteTypeView;
import com.zhiyouxing.travel.entity.vo.RouteTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 线路类型
 */
public interface RouteTypeService extends IService<RouteTypeEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<RouteTypeVO> selectListVO(Wrapper<RouteTypeEntity> wrapper);
   	
   	RouteTypeVO selectVO(@Param("ew") Wrapper<RouteTypeEntity> wrapper);
   	
   	List<RouteTypeView> selectListView(Wrapper<RouteTypeEntity> wrapper);
   	
   	RouteTypeView selectView(@Param("ew") Wrapper<RouteTypeEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<RouteTypeEntity> wrapper);

}

