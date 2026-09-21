package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.NewsTypeEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 旅游资讯分类
 */
@TableName("news_type")
public class NewsTypeView  extends NewsTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public NewsTypeView(){
	}
 
 	public NewsTypeView(NewsTypeEntity newsTypeEntity){
		BeanUtils.copyProperties(newsTypeEntity, this);
 		
	}


}
