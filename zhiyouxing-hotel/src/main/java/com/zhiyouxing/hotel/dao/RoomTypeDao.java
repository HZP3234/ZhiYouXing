package com.zhiyouxing.hotel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.hotel.entity.RoomTypeEntity;
import com.zhiyouxing.hotel.entity.view.RoomTypeView;
import com.zhiyouxing.hotel.entity.vo.RoomTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 客房类型
 */
public interface RoomTypeDao extends BaseMapper<RoomTypeEntity> {
	
	List<RoomTypeVO> selectListVO(@Param("ew") Wrapper<RoomTypeEntity> wrapper);
	
	RoomTypeVO selectVO(@Param("ew") Wrapper<RoomTypeEntity> wrapper);
	
	List<RoomTypeView> selectListView(@Param("ew") Wrapper<RoomTypeEntity> wrapper);

	List<RoomTypeView> selectListView(Page<?> page,@Param("ew") Wrapper<RoomTypeEntity> wrapper);

	
	RoomTypeView selectView(@Param("ew") Wrapper<RoomTypeEntity> wrapper);
	

}
