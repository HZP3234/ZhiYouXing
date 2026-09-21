package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.TourGuideEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 导游
 */
@TableName("tour_guide")
public class TourGuideView  extends TourGuideEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public TourGuideView(){
	}
 
 	public TourGuideView(TourGuideEntity tourGuideEntity){
		BeanUtils.copyProperties(tourGuideEntity, this);
 		
	}


}
