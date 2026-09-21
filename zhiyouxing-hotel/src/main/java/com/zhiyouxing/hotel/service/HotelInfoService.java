package com.zhiyouxing.hotel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import com.zhiyouxing.hotel.entity.view.HotelInfoView;
import com.zhiyouxing.hotel.entity.vo.HotelInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 酒店信息
 */
public interface HotelInfoService extends IService<HotelInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<HotelInfoVO> selectListVO(Wrapper<HotelInfoEntity> wrapper);
   	
   	HotelInfoVO selectVO(@Param("ew") Wrapper<HotelInfoEntity> wrapper);
   	
   	List<HotelInfoView> selectListView(Wrapper<HotelInfoEntity> wrapper);
   	
   	HotelInfoView selectView(@Param("ew") Wrapper<HotelInfoEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<HotelInfoEntity> wrapper);

   	

}

