package com.zhiyouxing.attraction.entity.view;

import com.zhiyouxing.attraction.entity.AttractionCommentEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 热门景点评论表
 * 后端返回视图实体辅助类
 */
@TableName("attraction_comment")
public class AttractionCommentView  extends AttractionCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public AttractionCommentView(){
	}
 
 	public AttractionCommentView(AttractionCommentEntity attractionCommentEntity){
		BeanUtils.copyProperties(attractionCommentEntity, this);
 		
	}


}
