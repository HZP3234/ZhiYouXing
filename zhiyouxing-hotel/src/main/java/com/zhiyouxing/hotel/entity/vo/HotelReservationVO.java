package com.zhiyouxing.hotel.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
 

/**
 * 酒店预订
 */
public class HotelReservationVO  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 客房名称
	 */
	
	private String roomName;
		
	/**
	 * 客房图片
	 */
	
	private String roomImage;
		
	/**
	 * 客房价格
	 */
	
	private Integer roomPrice;
		
	/**
	 * 客房数量
	 */
	
	private Integer roomCount;
		
	/**
	 * 入住天数
	 */
	
	private Integer stayDays;
		
	/**
	 * 总金额
	 */
	
	private Double totalAmount;
		
	/**
	 * 酒店名称
	 */
	
	private String hotelName;
		
	/**
	 * 预约时间
	 */
		
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 
	private Date reservationTime;
		
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
	 * 是否支付
	 */
	
	private String isPay;
				
	
	/**
	 * 设置：客房名称
	 */
	 
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	/**
	 * 获取：客房名称
	 */
	public String getRoomName() {
		return roomName;
	}
				
	
	/**
	 * 设置：客房图片
	 */
	 
	public void setRoomImage(String roomImage) {
		this.roomImage = roomImage;
	}
	
	/**
	 * 获取：客房图片
	 */
	public String getRoomImage() {
		return roomImage;
	}
				
	
	/**
	 * 设置：客房价格
	 */
	 
	public void setRoomPrice(Integer roomPrice) {
		this.roomPrice = roomPrice;
	}
	
	/**
	 * 获取：客房价格
	 */
	public Integer getRoomPrice() {
		return roomPrice;
	}
				
	
	/**
	 * 设置：客房数量
	 */
	 
	public void setRoomCount(Integer roomCount) {
		this.roomCount = roomCount;
	}
	
	/**
	 * 获取：客房数量
	 */
	public Integer getRoomCount() {
		return roomCount;
	}
				
	
	/**
	 * 设置：入住天数
	 */
	 
	public void setStayDays(Integer stayDays) {
		this.stayDays = stayDays;
	}
	
	/**
	 * 获取：入住天数
	 */
	public Integer getStayDays() {
		return stayDays;
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
	 * 设置：酒店名称
	 */
	 
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}
	
	/**
	 * 获取：酒店名称
	 */
	public String getHotelName() {
		return hotelName;
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
