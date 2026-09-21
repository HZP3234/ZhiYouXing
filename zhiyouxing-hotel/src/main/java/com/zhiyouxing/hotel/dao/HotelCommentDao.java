package com.zhiyouxing.hotel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.hotel.entity.HotelCommentEntity;
import com.zhiyouxing.hotel.entity.view.HotelCommentView;
import com.zhiyouxing.hotel.entity.vo.HotelCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 酒店信息评论表
 */
public interface HotelCommentDao extends BaseMapper<HotelCommentEntity> {
	
	List<HotelCommentVO> selectListVO(@Param("ew") Wrapper<HotelCommentEntity> wrapper);
	
	HotelCommentVO selectVO(@Param("ew") Wrapper<HotelCommentEntity> wrapper);
	
	List<HotelCommentView> selectListView(@Param("ew") Wrapper<HotelCommentEntity> wrapper);

	List<HotelCommentView> selectListView(Page<?> page,@Param("ew") Wrapper<HotelCommentEntity> wrapper);

	
	HotelCommentView selectView(@Param("ew") Wrapper<HotelCommentEntity> wrapper);
	

}
