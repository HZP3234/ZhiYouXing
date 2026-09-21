package com.zhiyouxing.food.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.food.entity.RestaurantEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 美食餐厅
 */
@TableName("restaurant")
public class RestaurantView  extends RestaurantEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public RestaurantView(){
	}
 
 	public RestaurantView(RestaurantEntity restaurantEntity){
		BeanUtils.copyProperties(restaurantEntity, this);
	}


}
