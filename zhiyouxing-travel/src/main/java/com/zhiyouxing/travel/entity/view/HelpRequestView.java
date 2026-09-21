package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.HelpRequestEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
 

/**
 * 求救信息
 */
@TableName("help_request")
public class HelpRequestView extends HelpRequestEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public HelpRequestView(){
	}
 
 	public HelpRequestView(HelpRequestEntity helpRequestEntity){
		BeanUtils.copyProperties(helpRequestEntity, this);
 		
	}


}
