package com.zhiyouxing.travel.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
 

/**
 * 报团信息
 */
public class GroupTourVO  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 线路图片
	 */
	
	private String routeImage;
		
	/**
	 * 出发日期
	 */
		
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 
	private Date departureDate;
		
	/**
	 * 线路费用
	 */
	
	private Double routeFee;
		
	/**
	 * 报名人数
	 */
	
	private Integer signupCount;
		
	/**
	 * 报团金额
	 */
	
	private Double groupTourAmount;
		
	/**
	 * 导游工号
	 */
	
	private String guideNo;
		
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
	 * 报团时间
	 */
		
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 
	private Date groupTourTime;
		
	/**
	 * 是否支付
	 */
	
	private String isPay;
				
	
	/**
	 * 设置：线路图片
	 */
	 
	public void setRouteImage(String routeImage) {
		this.routeImage = routeImage;
	}
	
	/**
	 * 获取：线路图片
	 */
	public String getRouteImage() {
		return routeImage;
	}
				
	
	/**
	 * 设置：出发日期
	 */
	 
	public void setDepartureDate(Date departureDate) {
		this.departureDate = departureDate;
	}
	
	/**
	 * 获取：出发日期
	 */
	public Date getDepartureDate() {
		return departureDate;
	}
				
	
	/**
	 * 设置：线路费用
	 */
	 
	public void setRouteFee(Double routeFee) {
		this.routeFee = routeFee;
	}
	
	/**
	 * 获取：线路费用
	 */
	public Double getRouteFee() {
		return routeFee;
	}
				
	
	/**
	 * 设置：报名人数
	 */
	 
	public void setSignupCount(Integer signupCount) {
		this.signupCount = signupCount;
	}
	
	/**
	 * 获取：报名人数
	 */
	public Integer getSignupCount() {
		return signupCount;
	}
				
	
	/**
	 * 设置：报团金额
	 */
	 
	public void setGroupTourAmount(Double groupTourAmount) {
		this.groupTourAmount = groupTourAmount;
	}
	
	/**
	 * 获取：报团金额
	 */
	public Double getGroupTourAmount() {
		return groupTourAmount;
	}
				
	
	/**
	 * 设置：导游工号
	 */
	 
	public void setGuideNo(String guideNo) {
		this.guideNo = guideNo;
	}
	
	/**
	 * 获取：导游工号
	 */
	public String getGuideNo() {
		return guideNo;
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
	 * 设置：报团时间
	 */
	 
	public void setGroupTourTime(Date groupTourTime) {
		this.groupTourTime = groupTourTime;
	}
	
	/**
	 * 获取：报团时间
	 */
	public Date getGroupTourTime() {
		return groupTourTime;
	}
				
	
	/**
	 * 设置：是否支付
	 */
	 
	public void setIsPay(String isPay) {
		this.isPay = isPay;
	}
	
	/**
	 * 获取：是否支付
	 */
	public String getIsPay() {
		return isPay;
	}
			
}
