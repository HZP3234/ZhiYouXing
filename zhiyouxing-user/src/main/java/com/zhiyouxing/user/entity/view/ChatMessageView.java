package com.zhiyouxing.user.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.user.entity.ChatMessageEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;


/**
 * 消息表
 */
@TableName("chat_message")
public class ChatMessageView  extends ChatMessageEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public ChatMessageView(){
	}
 
 	public ChatMessageView(ChatMessageEntity chatMessageEntity){
		BeanUtils.copyProperties(chatMessageEntity, this);
	}


}
