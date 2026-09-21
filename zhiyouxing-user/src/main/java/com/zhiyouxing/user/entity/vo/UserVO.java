package com.zhiyouxing.user.entity.vo;

import java.io.Serializable;

/**
 * 用户
 */
public class UserVO  implements Serializable {
	private static final long serialVersionUID = 1L;
	 			
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
