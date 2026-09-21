package com.zhiyouxing.attraction.service;

import com.zhiyouxing.attraction.entity.AttractionTypeEntity;
import com.zhiyouxing.attraction.entity.view.AttractionTypeView;
import com.zhiyouxing.attraction.entity.vo.AttractionTypeVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 景点类型
 */
public interface AttractionTypeService extends IService<AttractionTypeEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<AttractionTypeVO> selectListVO(Wrapper<AttractionTypeEntity> wrapper);
   	
   	AttractionTypeVO selectVO(@Param("ew") Wrapper<AttractionTypeEntity> wrapper);
   	
   	List<AttractionTypeView> selectListView(Wrapper<AttractionTypeEntity> wrapper);
   	
   	AttractionTypeView selectView(@Param("ew") Wrapper<AttractionTypeEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<AttractionTypeEntity> wrapper);

   	

}

