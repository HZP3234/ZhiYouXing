package com.zhiyouxing.hotel.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.hotel.entity.HotelCommentEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 酒店信息评论表
 */
@TableName("hotel_comment")
public class HotelCommentView  extends HotelCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public HotelCommentView(){
	}
 
 	public HotelCommentView(HotelCommentEntity hotelCommentEntity){
		BeanUtils.copyProperties(hotelCommentEntity, this);

	}


}
