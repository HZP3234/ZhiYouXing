package com.zhiyouxing.hotel.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 酒店信息
 */
@TableName("hotel_info")
public class HotelInfoView  extends HotelInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public HotelInfoView(){
	}
 
 	public HotelInfoView(HotelInfoEntity hotelInfoEntity){
		BeanUtils.copyProperties(hotelInfoEntity, this);

	}


}
