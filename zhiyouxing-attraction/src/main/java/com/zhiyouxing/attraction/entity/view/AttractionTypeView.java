package com.zhiyouxing.attraction.entity.view;

import com.zhiyouxing.attraction.entity.AttractionTypeEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 景点类型
 * 后端返回视图实体辅助类
 */
@TableName("attraction_type")
public class AttractionTypeView  extends AttractionTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public AttractionTypeView(){
	}
 
 	public AttractionTypeView(AttractionTypeEntity attractionTypeEntity){
		BeanUtils.copyProperties(attractionTypeEntity, this);
 		
	}


}
