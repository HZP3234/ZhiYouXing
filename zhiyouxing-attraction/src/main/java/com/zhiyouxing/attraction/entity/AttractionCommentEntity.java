package com.zhiyouxing.attraction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 热门景点评论表
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("attraction_comment")
public class AttractionCommentEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public AttractionCommentEntity() {
		
	}
	
	public AttractionCommentEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 关联表id
	 */
					
	private Long refId;
	
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
	 * 评分
	 */
					
	private Double score;
	
	/**
	 * 回复内容
	 */
					
	private String reply;
	
	
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
	 * 设置：关联表id
	 */
	public void setRefId(Long refId) {
		this.refId = refId;
	}
	/**
	 * 获取：关联表id
	 */
	public Long getRefId() {
		return refId;
	}
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
	 * 设置：评分
	 */
	public void setScore(Double score) {
		this.score = score;
	}
	/**
	 * 获取：评分
	 */
	public Double getScore() {
		return score;
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
