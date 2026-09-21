package com.zhiyouxing.travel.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.travel.entity.GroupTourEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 报团信息
 */
@TableName("group_tour")
public class GroupTourView  extends GroupTourEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public GroupTourView(){
	}
 
 	public GroupTourView(GroupTourEntity groupTourEntity){
		BeanUtils.copyProperties(groupTourEntity, this);
 		
	}


}
