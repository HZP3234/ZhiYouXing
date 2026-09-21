package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.ForumEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 交流论坛
 */
@TableName("forum")
public class ForumView  extends ForumEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public ForumView(){
	}
 
 	public ForumView(ForumEntity forumEntity){
		BeanUtils.copyProperties(forumEntity, this);
 		
	}
}
