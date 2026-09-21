package com.zhiyouxing.user.entity.vo;

import java.io.Serializable;

/**
 * 消息表
 */
public class ChatMessageVO  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 好友用户ID
	 */
	private Long fid;
		
	/**
	 * 内容
	 */
	private String content;
		
	/**
	 * 格式(1:文字，2:图片)
	 */
	private Integer format;
		
	/**
	 * 消息已读(0:未读，1:已读)
	 */
	private Integer isRead;

	/**
	 * 设置：好友用户ID
	 */
	 
	public void setFid(Long fid) {
		this.fid = fid;
	}
	
	/**
	 * 获取：好友用户ID
	 */
	public Long getFid() {
		return fid;
	}
				
	
	/**
	 * 设置：内容
	 */
	 
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * 获取：内容
	 */
	public String getContent() {
		return content;
	}
				
	
	/**
	 * 设置：格式(1:文字，2:图片)
	 */
	 
	public void setFormat(Integer format) {
		this.format = format;
	}
	
	/**
	 * 获取：格式(1:文字，2:图片)
	 */
	public Integer getFormat() {
		return format;
	}
				
	
	/**
	 * 设置：消息已读(0:未读，1:已读)
	 */
	 
	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}
	
	/**
	 * 获取：消息已读(0:未读，1:已读)
	 */
	public Integer getIsRead() {
		return isRead;
	}
			
}
