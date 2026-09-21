package com.zhiyouxing.hotel.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.hotel.entity.RoomTypeEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 客房类型
 */
@TableName("room_type")
public class RoomTypeView  extends RoomTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public RoomTypeView(){
	}
 
 	public RoomTypeView(RoomTypeEntity roomTypeEntity){
		BeanUtils.copyProperties(roomTypeEntity, this);

	}


}
