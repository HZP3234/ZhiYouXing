package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.HelpRequestEntity;
import com.zhiyouxing.travel.entity.view.HelpRequestView;
import com.zhiyouxing.travel.entity.vo.HelpRequestVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 求救信息
 */
public interface HelpRequestDao extends BaseMapper<HelpRequestEntity> {
	
	List<HelpRequestVO> selectListVO(@Param("ew") Wrapper<HelpRequestEntity> wrapper);
	
	HelpRequestVO selectVO(@Param("ew") Wrapper<HelpRequestEntity> wrapper);
	
	List<HelpRequestView> selectListView(@Param("ew") Wrapper<HelpRequestEntity> wrapper);

	List<HelpRequestView> selectListView(Page<?> page,@Param("ew") Wrapper<HelpRequestEntity> wrapper);

	
	HelpRequestView selectView(@Param("ew") Wrapper<HelpRequestEntity> wrapper);
	

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<HelpRequestEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<HelpRequestEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<HelpRequestEntity> wrapper);



}
