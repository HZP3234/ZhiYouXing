package com.zhiyouxing.attraction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 热门景点
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("attraction")
public class AttractionEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public AttractionEntity() {
		
	}
	
	public AttractionEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
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
	
	
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date addTime;

	/**
	 * 待审核 / 已通过 / 已驳回。景点前台一经手就重置成待审核
	 */
	private String auditStatus;

	/**
	 * 审核回复。驳回时是原因，会回显到景点前台的景点信息页
	 */
	private String auditReply;

	/**
	 * 审核时间
	 */
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date auditTime;

	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	/**
	 * 设置：审核状态
	 */
	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}
	/**
	 * 获取：审核状态
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
	/**
	 * 设置：审核时间
	 */
	public void setAuditTime(Date auditTime) {
		this.auditTime = auditTime;
	}
	/**
	 * 获取：审核时间
	 */
	public Date getAuditTime() {
		return auditTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
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
