package com.zhiyouxing.attraction.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
 

/**
 * 门票订单
 */
public class TicketOrderVO  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 景点名称
	 */
	
	private String attractionName;
		
	/**
	 * 图片
	 */
	
	private String image;
		
	/**
	 * 景点类型
	 */
	
	private String attractionType;
		
	/**
	 * 门票价格
	 */
	
	private Double ticketPrice;
		
	/**
	 * 购买数量
	 */
	
	private Integer quantity;
		
	/**
	 * 总金额
	 */
	
	private Double totalAmount;
		
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
	 * 购买时间
	 */
		
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 
	private Date purchaseTime;
		
	/**
	 * 是否支付
	 */
	
	private String isPay;
				
	
	/**
	 * 设置：景点名称
	 */
	 
	public void setAttractionName(String attractionName) {
		this.attractionName = attractionName;
	}
	
	/**
	 * 获取：景点名称
	 */
	public String getAttractionName() {
		return attractionName;
	}
				
	
	/**
	 * 设置：图片
	 */
	 
	public void setImage(String image) {
		this.image = image;
	}
	
	/**
	 * 获取：图片
	 */
	public String getImage() {
		return image;
	}
				
	
	/**
	 * 设置：景点类型
	 */
	 
	public void setAttractionType(String attractionType) {
		this.attractionType = attractionType;
	}
	
	/**
	 * 获取：景点类型
	 */
	public String getAttractionType() {
		return attractionType;
	}
				
	
	/**
	 * 设置：门票价格
	 */
	 
	public void setTicketPrice(Double ticketPrice) {
		this.ticketPrice = ticketPrice;
	}
	
	/**
	 * 获取：门票价格
	 */
	public Double getTicketPrice() {
		return ticketPrice;
	}
				
	
	/**
	 * 设置：购买数量
	 */
	 
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * 获取：购买数量
	 */
	public Integer getQuantity() {
		return quantity;
	}
				
	
	/**
	 * 设置：总金额
	 */
	 
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	/**
	 * 获取：总金额
	 */
	public Double getTotalAmount() {
		return totalAmount;
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
	 * 设置：购买时间
	 */
	 
	public void setPurchaseTime(Date purchaseTime) {
		this.purchaseTime = purchaseTime;
	}
	
	/**
	 * 获取：购买时间
	 */
	public Date getPurchaseTime() {
		return purchaseTime;
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
