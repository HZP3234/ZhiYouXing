package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.NewsEntity;
import com.zhiyouxing.travel.entity.view.NewsView;
import com.zhiyouxing.travel.entity.vo.NewsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 旅游资讯
 */
public interface NewsDao extends BaseMapper<NewsEntity> {
	
	List<NewsVO> selectListVO(@Param("ew") Wrapper<NewsEntity> wrapper);
	
	NewsVO selectVO(@Param("ew") Wrapper<NewsEntity> wrapper);
	
	List<NewsView> selectListView(@Param("ew") Wrapper<NewsEntity> wrapper);

	List<NewsView> selectListView(Page<?> page,@Param("ew") Wrapper<NewsEntity> wrapper);

	
	NewsView selectView(@Param("ew") Wrapper<NewsEntity> wrapper);
	

}
