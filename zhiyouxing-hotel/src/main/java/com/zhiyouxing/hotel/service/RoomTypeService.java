package com.zhiyouxing.hotel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.hotel.entity.RoomTypeEntity;
import com.zhiyouxing.hotel.entity.view.RoomTypeView;
import com.zhiyouxing.hotel.entity.vo.RoomTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 客房类型
 */
public interface RoomTypeService extends IService<RoomTypeEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<RoomTypeVO> selectListVO(Wrapper<RoomTypeEntity> wrapper);
   	
   	RoomTypeVO selectVO(@Param("ew") Wrapper<RoomTypeEntity> wrapper);
   	
   	List<RoomTypeView> selectListView(Wrapper<RoomTypeEntity> wrapper);
   	
   	RoomTypeView selectView(@Param("ew") Wrapper<RoomTypeEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<RoomTypeEntity> wrapper);
}

