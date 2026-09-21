package com.zhiyouxing.travel.entity.view;

import com.zhiyouxing.travel.entity.FriendLinkEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 友情链接
 */
@TableName("friend_link")
public class FriendLinkView  extends FriendLinkEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public FriendLinkView(){
	}
 
 	public FriendLinkView(FriendLinkEntity friendLinkEntity){
		BeanUtils.copyProperties(friendLinkEntity, this);
 		
	}
}
