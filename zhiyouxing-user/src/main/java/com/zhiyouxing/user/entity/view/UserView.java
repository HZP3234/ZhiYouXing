package com.zhiyouxing.user.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.user.entity.UserEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 用户
 */
@TableName("user")
public class UserView  extends UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public UserView(){
	}
 
 	public UserView(UserEntity userEntity){
		BeanUtils.copyProperties(userEntity, this);
	}


}
