package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;
 

/**
 * 旅游线路评论表
 */
public class TravelRouteCommentModel  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 用户id
	 */
	
	private Long userId;
		
	/**
	 * 头像
	 */
	
	private String avatarUrl;
		
	/**
	 * 用户名
	 */
	
	private String nickname;
		
	/**
	 * 评论内容
	 */
	
	private String content;
		
	/**
	 * 回复内容
	 */
	
	private String reply;
				
	
	/**
	 * 设置：用户id
	 */
	 
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	/**
	 * 获取：用户id
	 */
	public Long getUserId() {
		return userId;
	}
				
	
	/**
	 * 设置：头像
	 */
	 
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	
	/**
	 * 获取：头像
	 */
	public String getAvatarUrl() {
		return avatarUrl;
	}
				
	
	/**
	 * 设置：用户名
	 */
	 
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	/**
	 * 获取：用户名
	 */
	public String getNickname() {
		return nickname;
	}
				
	
	/**
	 * 设置：评论内容
	 */
	 
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * 获取：评论内容
	 */
	public String getContent() {
		return content;
	}
				
	
	/**
	 * 设置：回复内容
	 */
	 
	public void setReply(String reply) {
		this.reply = reply;
	}
	
	/**
	 * 获取：回复内容
	 */
	public String getReply() {
		return reply;
	}
			
}
