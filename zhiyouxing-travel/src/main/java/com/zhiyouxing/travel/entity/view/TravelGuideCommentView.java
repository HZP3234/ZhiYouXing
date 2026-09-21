package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.TravelGuideCommentEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 旅游攻略评论表
 */
@TableName("travel_guide_comment")
public class TravelGuideCommentView  extends TravelGuideCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public TravelGuideCommentView(){
	}
 
 	public TravelGuideCommentView(TravelGuideCommentEntity travelGuideCommentEntity){
		BeanUtils.copyProperties(travelGuideCommentEntity, this);
 		
	}


}
