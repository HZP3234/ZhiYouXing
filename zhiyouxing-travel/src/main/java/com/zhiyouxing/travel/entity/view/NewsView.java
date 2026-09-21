package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.NewsEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 旅游资讯
 */
@TableName("news")
public class NewsView  extends NewsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public NewsView(){
	}
 
 	public NewsView(NewsEntity newsEntity){
		BeanUtils.copyProperties(newsEntity, this);
 		
	}


}
