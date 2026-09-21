package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.TravelRouteCommentEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 旅游线路评论表
 */
@TableName("travel_route_comment")
public class TravelRouteCommentView  extends TravelRouteCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public TravelRouteCommentView(){
	}
 
 	public TravelRouteCommentView(TravelRouteCommentEntity travelRouteCommentEntity){
		BeanUtils.copyProperties(travelRouteCommentEntity, this);
 		
	}
}
