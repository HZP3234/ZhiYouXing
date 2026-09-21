package com.zhiyouxing.common.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serializable;
import java.util.Date;

/** 
 * token表
 */
@TableName("token")
public class TokenEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@TableId(type = IdType.AUTO)
	private Long id;
	
	/**
	 * 用户id
	 */
	private Long userId;
	
	/**
	 * 用户名
	 */
	private String username;
	
	/**
	 * 表名
	 */
	private String tableName;
	
	/**
	 * 角色
	 */
	private String role;
	
	/**
	 * token
	 */
	private String token;
	
	/**
	 * 过期时间
	 */
	private Date expiratedTime;
	
	/**
	 * 新增时间
	 */
	private Date addTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpiratedTime() {
		return expiratedTime;
	}

	public void setExpiratedTime(Date expiratedTime) {
		this.expiratedTime = expiratedTime;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public TokenEntity(Long userId, String username, String tableName,String role, String token, Date expiratedTime) {
		super();
		this.userId = userId;
		this.username = username;
		this.tableName = tableName;
		this.role = role;
		this.token = token;
		this.expiratedTime = expiratedTime;
	}
	
	public TokenEntity() {
	}
	
}
