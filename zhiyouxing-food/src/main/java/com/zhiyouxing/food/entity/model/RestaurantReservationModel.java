package com.zhiyouxing.food.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 餐厅预约
 */
public class RestaurantReservationModel  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 餐厅图片
	 */
	
	private String restaurantImage;
		
	/**
	 * 餐厅类型
	 */
	
	private String restaurantType;
		
	/**
	 * 用餐人数
	 */
	
	private Integer dinerCount;
		
	/**
	 * 预约时间
	 */
		
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 
	private Date reservationTime;
		
	/**
	 * 用餐备注
	 */
	
	private String diningRemark;
		
	/**
	 * 用户账号
	 */
	
	private String userAccount;
		
	/**
	 * 用户姓名
	 */
	
	private String userName;
		
	/**
	 * 联系方式
	 */
	
	private String contactPhone;
		
	/**
	 * 是否审核
	 */
	
	private String auditStatus;
		
	/**
	 * 审核回复
	 */
	
	private String auditReply;
				
	
	/**
	 * 设置：餐厅图片
	 */
	 
	public void setRestaurantImage(String restaurantImage) {
		this.restaurantImage = restaurantImage;
	}
	
	/**
	 * 获取：餐厅图片
	 */
	public String getRestaurantImage() {
		return restaurantImage;
	}
				
	
	/**
	 * 设置：餐厅类型
	 */
	 
	public void setRestaurantType(String restaurantType) {
		this.restaurantType = restaurantType;
	}
	
	/**
	 * 获取：餐厅类型
	 */
	public String getRestaurantType() {
		return restaurantType;
	}
				
	
	/**
	 * 设置：用餐人数
	 */
	 
	public void setDinerCount(Integer dinerCount) {
		this.dinerCount = dinerCount;
	}
	
	/**
	 * 获取：用餐人数
	 */
	public Integer getDinerCount() {
		return dinerCount;
	}
				
	
	/**
	 * 设置：预约时间
	 */
	 
	public void setReservationTime(Date reservationTime) {
		this.reservationTime = reservationTime;
	}
	
	/**
	 * 获取：预约时间
	 */
	public Date getReservationTime() {
		return reservationTime;
	}
				
	
	/**
	 * 设置：用餐备注
	 */
	 
	public void setDiningRemark(String diningRemark) {
		this.diningRemark = diningRemark;
	}
	
	/**
	 * 获取：用餐备注
	 */
	public String getDiningRemark() {
		return diningRemark;
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
	 * 设置：是否审核
	 */
	 
	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}
	
	/**
	 * 获取：是否审核
	 */
	public String getAuditStatus() {
		return auditStatus;
	}
				
	
	/**
	 * 设置：审核回复
	 */
	 
	public void setAuditReply(String auditReply) {
		this.auditReply = auditReply;
	}
	
	/**
	 * 获取：审核回复
	 */
	public String getAuditReply() {
		return auditReply;
	}
			
}
