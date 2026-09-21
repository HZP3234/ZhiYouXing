package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.GroupTourEntity;
import com.zhiyouxing.travel.entity.view.GroupTourView;
import com.zhiyouxing.travel.entity.vo.GroupTourVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 报团信息
 */
public interface GroupTourService extends IService<GroupTourEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<GroupTourVO> selectListVO(Wrapper<GroupTourEntity> wrapper);
   	
   	GroupTourVO selectVO(@Param("ew") Wrapper<GroupTourEntity> wrapper);
   	
   	List<GroupTourView> selectListView(Wrapper<GroupTourEntity> wrapper);
   	
   	GroupTourView selectView(@Param("ew") Wrapper<GroupTourEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<GroupTourEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<GroupTourEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<GroupTourEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<GroupTourEntity> wrapper);

}

