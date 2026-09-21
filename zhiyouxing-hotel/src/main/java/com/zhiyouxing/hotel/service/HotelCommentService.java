package com.zhiyouxing.hotel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.hotel.entity.HotelCommentEntity;
import com.zhiyouxing.hotel.entity.view.HotelCommentView;
import com.zhiyouxing.hotel.entity.vo.HotelCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 酒店信息评论表
 */
public interface HotelCommentService extends IService<HotelCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<HotelCommentVO> selectListVO(Wrapper<HotelCommentEntity> wrapper);
   	
   	HotelCommentVO selectVO(@Param("ew") Wrapper<HotelCommentEntity> wrapper);
   	
   	List<HotelCommentView> selectListView(Wrapper<HotelCommentEntity> wrapper);
   	
   	HotelCommentView selectView(@Param("ew") Wrapper<HotelCommentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<HotelCommentEntity> wrapper);

   	

}

