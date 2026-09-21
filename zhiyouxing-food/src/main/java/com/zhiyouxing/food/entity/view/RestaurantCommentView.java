package com.zhiyouxing.food.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.food.entity.RestaurantCommentEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 美食餐厅评论表
 */
@TableName("restaurant_comment")
public class RestaurantCommentView  extends RestaurantCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public RestaurantCommentView(){
	}
 
 	public RestaurantCommentView(RestaurantCommentEntity restaurantCommentEntity){
		BeanUtils.copyProperties(restaurantCommentEntity, this);
	}


}
