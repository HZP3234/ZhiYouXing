package com.zhiyouxing.attraction.entity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
 

/**
 * 热门景点
 */
public class AttractionModel  implements Serializable {
	private static final long serialVersionUID = 1L;

	 			
	/**
	 * 图片
	 */
	
	private String image;
		
	/**
	 * 景点类型
	 */
	
	private String attractionType;
		
	/**
	 * 开放时间
	 */
	
	private String openingHours;
		
	/**
	 * 门票价格
	 */
	
	private Double ticketPrice;
		
	/**
	 * 数量
	 */
	
	private Integer quantity;
		
	/**
	 * 景点位置
	 */
	
	private String attractionLocation;
		
	/**
	 * 景点介绍
	 */
	
	private String attractionDescription;
		
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
	 * 评论数
	 */
	
	private Integer discussNum;
		
	/**
	 * 收藏数
	 */
	
	private Integer storeUpNum;
				
	
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
	 * 设置：开放时间
	 */
	 
	public void setOpeningHours(String openingHours) {
		this.openingHours = openingHours;
	}
	
	/**
	 * 获取：开放时间
	 */
	public String getOpeningHours() {
		return openingHours;
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
	 * 设置：数量
	 */
	 
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * 获取：数量
	 */
	public Integer getQuantity() {
		return quantity;
	}
				
	
	/**
	 * 设置：景点位置
	 */
	 
	public void setAttractionLocation(String attractionLocation) {
		this.attractionLocation = attractionLocation;
	}
	
	/**
	 * 获取：景点位置
	 */
	public String getAttractionLocation() {
		return attractionLocation;
	}
				
	
	/**
	 * 设置：景点介绍
	 */
	 
	public void setAttractionDescription(String attractionDescription) {
		this.attractionDescription = attractionDescription;
	}
	
	/**
	 * 获取：景点介绍
	 */
	public String getAttractionDescription() {
		return attractionDescription;
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
