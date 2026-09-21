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
 * 好友表
 */
@TableName("friend")
public class FriendEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	public FriendEntity() {
		
	}
	
	public FriendEntity(T t) {
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
	 * 名称
	 */
					
	private String name;
	
	/**
	 * 图片
	 */
					
	private String picture;
	
	/**
	 * 角色
	 */
					
	private String role;
	
	/**
	 * 表名
	 */
					
	private String tableName;
	
	/**
	 * 别名
	 */
					
	private String alias;
	
	/**
	 * 类型(0:好友申请，1:好友，2:消息)
	 */
					
	private Integer type;
	
	
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
	 * 设置：名称
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取：名称
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置：图片
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}
	/**
	 * 获取：图片
	 */
	public String getPicture() {
		return picture;
	}
	/**
	 * 设置：角色
	 */
	public void setRole(String role) {
		this.role = role;
	}
	/**
	 * 获取：角色
	 */
	public String getRole() {
		return role;
	}
	/**
	 * 设置：表名
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * 获取：表名
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * 设置：别名
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	/**
	 * 获取：别名
	 */
	public String getAlias() {
		return alias;
	}
	/**
	 * 设置：类型(0:好友申请，1:好友，2:消息)
	 */
	public void setType(Integer type) {
		this.type = type;
	}
	/**
	 * 获取：类型(0:好友申请，1:好友，2:消息)
	 */
	public Integer getType() {
		return type;
	}

}
