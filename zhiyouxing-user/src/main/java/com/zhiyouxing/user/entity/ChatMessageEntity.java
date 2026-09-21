package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 消息表
 */
@TableName("chat_message")
public class ChatMessageEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	public ChatMessageEntity() {

	}

	public ChatMessageEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}

	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 用户ID
	 */

	private Long uid;

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


	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date addTime;

	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 设置：用户ID
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}
	/**
	 * 获取：用户ID
	 */
	public Long getUid() {
		return uid;
	}
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
