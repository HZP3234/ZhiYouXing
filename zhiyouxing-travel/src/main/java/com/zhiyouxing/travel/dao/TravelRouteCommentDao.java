package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.TravelRouteCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteCommentView;
import com.zhiyouxing.travel.entity.vo.TravelRouteCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 旅游线路评论表
 */
public interface TravelRouteCommentDao extends BaseMapper<TravelRouteCommentEntity> {
	
	List<TravelRouteCommentVO> selectListVO(@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);
	
	TravelRouteCommentVO selectVO(@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);
	
	List<TravelRouteCommentView> selectListView(@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);

	List<TravelRouteCommentView> selectListView(Page<?> page,@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);

	
	TravelRouteCommentView selectView(@Param("ew") Wrapper<TravelRouteCommentEntity> wrapper);
	

}
