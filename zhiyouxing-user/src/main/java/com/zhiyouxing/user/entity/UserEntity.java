package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户
 * 数据库通用操作实体类（普通增删改查）
 * @author 
 * @email 
 * @date 2024-02-25 14:46:55
 */
@TableName("user")
public class UserEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	public UserEntity() {
		
	}
	
	public UserEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId
    private Long id;
	/**
	 * 用户账号
	 */
					
	private String userAccount;
	
	/**
	 * 密码
	 */
					
	private String password;
	
	/**
	 * 用户姓名
	 */
					
	private String userName;
	
	/**
	 * 头像
	 */
					
	private String avatar;
	
	/**
	 * 性别
	 */
					
	private String gender;
	
	/**
	 * 联系方式
	 */
					
	private String contactPhone;
	
	/**
	 * 状态
	 */
					
	private Integer status;
	
	/**
	 * 密码错误次数
	 */
					
	private Integer passwordWrongNum;
	
	
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
	 * 设置：用户账号
	 */
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	/**
	 * 获取：用户账号
	 */
	public String getUserAccount() {
		return userAccount;
	}
	/**
	 * 设置：密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * 获取：密码
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * 设置：用户姓名
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 获取：用户姓名
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置：头像
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	/**
	 * 获取：头像
	 */
	public String getAvatar() {
		return avatar;
	}
	/**
	 * 设置：性别
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	/**
	 * 获取：性别
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * 设置：联系方式
	 */
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	/**
	 * 获取：联系方式
	 */
	public String getContactPhone() {
		return contactPhone;
	}
	/**
	 * 设置：状态
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 获取：状态
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * 设置：密码错误次数
	 */
	public void setPasswordWrongNum(Integer passwordWrongNum) {
		this.passwordWrongNum = passwordWrongNum;
	}
	/**
	 * 获取：密码错误次数
	 */
	public Integer getPasswordWrongNum() {
		return passwordWrongNum;
	}

}
