package com.zhiyouxing.user.entity.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhiyouxing.user.entity.FriendEntity;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 好友表
 */
@TableName("friend")
public class FriendView  extends FriendEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public FriendView(){
	}
 
 	public FriendView(FriendEntity friendEntity){
		BeanUtils.copyProperties(friendEntity, this);
	}

    private String content;

    private int notreadnum;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNotreadnum() {
        return notreadnum;
    }

    public void setNotreadnum(int notreadnum) {
        this.notreadnum = notreadnum;
    }
}
