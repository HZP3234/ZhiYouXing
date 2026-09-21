package com.zhiyouxing.hotel.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.hotel.entity.HotelReservationEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 酒店预订
 */
@TableName("hotel_reservation")
public class HotelReservationView  extends HotelReservationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public HotelReservationView(){
	}
 
 	public HotelReservationView(HotelReservationEntity hotelReservationEntity){
		BeanUtils.copyProperties(hotelReservationEntity, this);

	}


}
