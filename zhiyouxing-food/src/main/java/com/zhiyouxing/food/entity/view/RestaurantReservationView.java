package com.zhiyouxing.food.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.food.entity.RestaurantReservationEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 餐厅预约
 */
@TableName("restaurant_reservation")
public class RestaurantReservationView  extends RestaurantReservationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public RestaurantReservationView(){
	}
 
 	public RestaurantReservationView(RestaurantReservationEntity restaurantReservationEntity){
		BeanUtils.copyProperties(restaurantReservationEntity, this);
	}


}
