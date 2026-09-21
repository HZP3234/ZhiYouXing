package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.RouteTypeEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 线路类型
 */
@TableName("route_type")
public class RouteTypeView  extends RouteTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public RouteTypeView(){
	}
 
 	public RouteTypeView(RouteTypeEntity routeTypeEntity){
		BeanUtils.copyProperties(routeTypeEntity, this);
 		
	}


}
