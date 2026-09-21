package com.zhiyouxing.attraction.service;

import com.zhiyouxing.attraction.entity.AttractionEntity;
import com.zhiyouxing.attraction.entity.view.AttractionView;
import com.zhiyouxing.attraction.entity.vo.AttractionVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 热门景点
 */
public interface AttractionService extends IService<AttractionEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<AttractionVO> selectListVO(Wrapper<AttractionEntity> wrapper);
   	
   	AttractionVO selectVO(@Param("ew") Wrapper<AttractionEntity> wrapper);
   	
   	List<AttractionView> selectListView(Wrapper<AttractionEntity> wrapper);
   	
   	AttractionView selectView(@Param("ew") Wrapper<AttractionEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<AttractionEntity> wrapper);

   	

}

