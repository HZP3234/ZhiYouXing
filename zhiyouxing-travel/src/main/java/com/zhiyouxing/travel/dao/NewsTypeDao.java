package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.NewsTypeEntity;
import com.zhiyouxing.travel.entity.view.NewsTypeView;
import com.zhiyouxing.travel.entity.vo.NewsTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 旅游资讯分类
 */
public interface NewsTypeDao extends BaseMapper<NewsTypeEntity> {
	
	List<NewsTypeVO> selectListVO(@Param("ew") Wrapper<NewsTypeEntity> wrapper);
	
	NewsTypeVO selectVO(@Param("ew") Wrapper<NewsTypeEntity> wrapper);
	
	List<NewsTypeView> selectListView(@Param("ew") Wrapper<NewsTypeEntity> wrapper);

	List<NewsTypeView> selectListView(Page<?> page,@Param("ew") Wrapper<NewsTypeEntity> wrapper);

	
	NewsTypeView selectView(@Param("ew") Wrapper<NewsTypeEntity> wrapper);
	

}
