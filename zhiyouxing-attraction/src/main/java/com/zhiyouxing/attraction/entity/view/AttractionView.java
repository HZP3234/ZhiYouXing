package com.zhiyouxing.attraction.entity.view;

import com.zhiyouxing.attraction.entity.AttractionEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 热门景点
 * 后端返回视图实体辅助类
 */
@TableName("attraction")
public class AttractionView  extends AttractionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public AttractionView(){
	}
 
 	public AttractionView(AttractionEntity attractionEntity){
		BeanUtils.copyProperties(attractionEntity, this);
 		
	}


}
