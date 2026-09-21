package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.ForumEntity;
import com.zhiyouxing.travel.entity.view.ForumView;
import com.zhiyouxing.travel.entity.vo.ForumVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 交流论坛
 */
public interface ForumDao extends BaseMapper<ForumEntity> {
	
	List<ForumVO> selectListVO(@Param("ew") Wrapper<ForumEntity> wrapper);
	
	ForumVO selectVO(@Param("ew") Wrapper<ForumEntity> wrapper);
	
	List<ForumView> selectListView(@Param("ew") Wrapper<ForumEntity> wrapper);

	List<ForumView> selectListView(Page<?> page,@Param("ew") Wrapper<ForumEntity> wrapper);

	
	ForumView selectView(@Param("ew") Wrapper<ForumEntity> wrapper);
	

}
