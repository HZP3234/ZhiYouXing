package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.ForumEntity;
import com.zhiyouxing.travel.entity.view.ForumView;
import com.zhiyouxing.travel.entity.vo.ForumVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 交流论坛
 */
public interface ForumService extends IService<ForumEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<ForumVO> selectListVO(Wrapper<ForumEntity> wrapper);
   	
   	ForumVO selectVO(@Param("ew") Wrapper<ForumEntity> wrapper);
   	
   	List<ForumView> selectListView(Wrapper<ForumEntity> wrapper);
   	
   	ForumView selectView(@Param("ew") Wrapper<ForumEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<ForumEntity> wrapper);

   	

}

