package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.HelpRequestEntity;
import com.zhiyouxing.travel.entity.view.HelpRequestView;
import com.zhiyouxing.travel.entity.vo.HelpRequestVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 求救信息
 */
public interface HelpRequestService extends IService<HelpRequestEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<HelpRequestVO> selectListVO(Wrapper<HelpRequestEntity> wrapper);
   	
   	HelpRequestVO selectVO(@Param("ew") Wrapper<HelpRequestEntity> wrapper);
   	
   	List<HelpRequestView> selectListView(Wrapper<HelpRequestEntity> wrapper);
   	
   	HelpRequestView selectView(@Param("ew") Wrapper<HelpRequestEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<HelpRequestEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<HelpRequestEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<HelpRequestEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<HelpRequestEntity> wrapper);

}
