package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.NewsTypeEntity;
import com.zhiyouxing.travel.entity.view.NewsTypeView;
import com.zhiyouxing.travel.entity.vo.NewsTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 旅游资讯分类
 */
public interface NewsTypeService extends IService<NewsTypeEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<NewsTypeVO> selectListVO(Wrapper<NewsTypeEntity> wrapper);
   	
   	NewsTypeVO selectVO(@Param("ew") Wrapper<NewsTypeEntity> wrapper);
   	
   	List<NewsTypeView> selectListView(Wrapper<NewsTypeEntity> wrapper);
   	
   	NewsTypeView selectView(@Param("ew") Wrapper<NewsTypeEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<NewsTypeEntity> wrapper);

}

