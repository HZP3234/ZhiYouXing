package com.zhiyouxing.hotel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import com.zhiyouxing.hotel.entity.view.HotelInfoView;
import com.zhiyouxing.hotel.entity.vo.HotelInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 酒店信息
 */
public interface HotelInfoDao extends BaseMapper<HotelInfoEntity> {
	
	List<HotelInfoVO> selectListVO(@Param("ew") Wrapper<HotelInfoEntity> wrapper);
	
	HotelInfoVO selectVO(@Param("ew") Wrapper<HotelInfoEntity> wrapper);
	
	List<HotelInfoView> selectListView(@Param("ew") Wrapper<HotelInfoEntity> wrapper);

	List<HotelInfoView> selectListView(Page<?> page,@Param("ew") Wrapper<HotelInfoEntity> wrapper);

	
	HotelInfoView selectView(@Param("ew") Wrapper<HotelInfoEntity> wrapper);
	

}
