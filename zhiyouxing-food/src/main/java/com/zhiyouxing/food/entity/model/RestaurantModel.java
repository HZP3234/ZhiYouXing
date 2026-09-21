package com.zhiyouxing.food.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 美食餐厅
 */
public class RestaurantModel  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 餐厅图片
	 */
	
	private String restaurantImage;
		
	/**
	 * 特色菜品
	 */
	
	private String signatureDish;
		
	/**
	 * 营业时间
	 */
	
	private String businessHours;
		
	/**
	 * 人均消费
	 */
	
	private String avgCost;
		
	/**
	 * 餐厅地址
	 */
	
	private String restaurantAddress;
		
	/**
	 * 联系电话
	 */
	
	private String contactPhone;
		
	/**
	 * 餐厅简介
	 */
	
	private String restaurantIntro;
		
	/**
	 * 赞
	 */
	
	private Integer thumbsUpNum;
		
	/**
	 * 踩
	 */
	
	private Integer crazilyNum;
		
	/**
	 * 最近点击时间
	 */
		
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 
	private Date clickTime;
		
	/**
	 * 点击次数
	 */
	
	private Integer clickNum;
		
	/**
	 * 评论数
	 */
	
	private Integer discussNum;
		
	/**
	 * 收藏数
	 */
	
	private Integer storeUpNum;
				
	
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
	 * 设置：特色菜品
	 */
	 
	public void setSignatureDish(String signatureDish) {
		this.signatureDish = signatureDish;
	}
	
	/**
	 * 获取：特色菜品
	 */
	public String getSignatureDish() {
		return signatureDish;
	}
				
	
	/**
	 * 设置：营业时间
	 */
	 
	public void setBusinessHours(String businessHours) {
		this.businessHours = businessHours;
	}
	
	/**
	 * 获取：营业时间
	 */
	public String getBusinessHours() {
		return businessHours;
	}
				
	
	/**
	 * 设置：人均消费
	 */
	 
	public void setAvgCost(String avgCost) {
		this.avgCost = avgCost;
	}
	
	/**
	 * 获取：人均消费
	 */
	public String getAvgCost() {
		return avgCost;
	}
				
	
	/**
	 * 设置：餐厅地址
	 */
	 
	public void setRestaurantAddress(String restaurantAddress) {
		this.restaurantAddress = restaurantAddress;
	}
	
	/**
	 * 获取：餐厅地址
	 */
	public String getRestaurantAddress() {
		return restaurantAddress;
	}
				
	
	/**
	 * 设置：联系电话
	 */
	 
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	
	/**
	 * 获取：联系电话
	 */
	public String getContactPhone() {
		return contactPhone;
	}
				
	
	/**
	 * 设置：餐厅简介
	 */
	 
	public void setRestaurantIntro(String restaurantIntro) {
		this.restaurantIntro = restaurantIntro;
	}
	
	/**
	 * 获取：餐厅简介
	 */
	public String getRestaurantIntro() {
		return restaurantIntro;
	}
				
	
	/**
	 * 设置：赞
	 */
	 
	public void setThumbsUpNum(Integer thumbsUpNum) {
		this.thumbsUpNum = thumbsUpNum;
	}
	
	/**
	 * 获取：赞
	 */
	public Integer getThumbsUpNum() {
		return thumbsUpNum;
	}
				
	
	/**
	 * 设置：踩
	 */
	 
	public void setCrazilyNum(Integer crazilyNum) {
		this.crazilyNum = crazilyNum;
	}
	
	/**
	 * 获取：踩
	 */
	public Integer getCrazilyNum() {
		return crazilyNum;
	}
				
	
	/**
	 * 设置：最近点击时间
	 */
	 
	public void setClickTime(Date clickTime) {
		this.clickTime = clickTime;
	}
	
	/**
	 * 获取：最近点击时间
	 */
	public Date getClickTime() {
		return clickTime;
	}
				
	
	/**
	 * 设置：点击次数
	 */
	 
	public void setClickNum(Integer clickNum) {
		this.clickNum = clickNum;
	}
	
	/**
	 * 获取：点击次数
	 */
	public Integer getClickNum() {
		return clickNum;
	}
				
	
	/**
	 * 设置：评论数
	 */
	 
	public void setDiscussNum(Integer discussNum) {
		this.discussNum = discussNum;
	}
	
	/**
	 * 获取：评论数
	 */
	public Integer getDiscussNum() {
		return discussNum;
	}
				
	
	/**
	 * 设置：收藏数
	 */
	 
	public void setStoreUpNum(Integer storeUpNum) {
		this.storeUpNum = storeUpNum;
	}
	
	/**
	 * 获取：收藏数
	 */
	public Integer getStoreUpNum() {
		return storeUpNum;
	}
			
}
