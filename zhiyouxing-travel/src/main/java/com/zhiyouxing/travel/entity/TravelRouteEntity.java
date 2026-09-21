package com.zhiyouxing.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 旅游线路
 */
@TableName("travel_route")
public class TravelRouteEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public TravelRouteEntity() {
		
	}
	
	public TravelRouteEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 线路名称
	 */
					
	private String routeName;
	
	/**
	 * 线路图片
	 */
					
	private String routeImage;
	
	/**
	 * 景点名称
	 */
					
	private String attractionName;
	
	/**
	 * 景点类型
	 */
					
	private String attractionType;
	
	/**
	 * 景点位置
	 */
					
	private String attractionLocation;
	
	/**
	 * 起点
	 */
					
	private String startPoint;
	
	/**
	 * 途径路段
	 */
					
	private String viaRoad;
	
	/**
	 * 终点
	 */
					
	private String endPoint;
	
	/**
	 * 线路费用
	 */
					
	private Double routeFee;
	
	/**
	 * 报团名额
	 */
					
	private Integer groupQuota;
	
	/**
	 * 出发日期
	 */
				
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 		
	private Date departureDate;
	
	/**
	 * 交通方式
	 */

	private String transportMode;

	/**
	 * 行程天数
	 */

	private Integer days;

	/**
	 * 线路详情
	 */
					
	private String routeDetail;
	
	/**
	 * 导游工号
	 */
					
	private String guideNo;
	
	/**
	 * 导游姓名
	 */
					
	private String guideName;
	
	/**
	 * 联系方式
	 */
					
	private String contactPhone;
	
	/**
	 * 导游介绍
	 */
					
	private String guideResume;
	
	/**
	 * 赞
	 */
					
	private Integer thumbsUpNum;
	
	/**
	 * 踩
	 */
					
	private Integer crazilyNum;
	
	/**
	 * 评论数
	 */
					
	private Integer discussNum;
	
	/**
	 * 收藏数
	 */
					
	private Integer storeUpNum;

	/**
	 * 审核状态：待审核 / 已通过 / 已驳回
	 */

	private String auditStatus;

	/**
	 * 审核回复（驳回原因）
	 */

	private String auditReply;

	/**
	 * 审核时间
	 */

	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date auditTime;


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
	 * 设置：线路名称
	 */
	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}
	/**
	 * 获取：线路名称
	 */
	public String getRouteName() {
		return routeName;
	}
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
	 * 设置：起点
	 */
	public void setStartPoint(String startPoint) {
		this.startPoint = startPoint;
	}
	/**
	 * 获取：起点
	 */
	public String getStartPoint() {
		return startPoint;
	}
	/**
	 * 设置：途径路段
	 */
	public void setViaRoad(String viaRoad) {
		this.viaRoad = viaRoad;
	}
	/**
	 * 获取：途径路段
	 */
	public String getViaRoad() {
		return viaRoad;
	}
	/**
	 * 设置：终点
	 */
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	/**
	 * 获取：终点
	 */
	public String getEndPoint() {
		return endPoint;
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
	 * 设置：报团名额
	 */
	public void setGroupQuota(Integer groupQuota) {
		this.groupQuota = groupQuota;
	}
	/**
	 * 获取：报团名额
	 */
	public Integer getGroupQuota() {
		return groupQuota;
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
	 * 设置：交通方式
	 */
	public void setTransportMode(String transportMode) {
		this.transportMode = transportMode;
	}
	/**
	 * 获取：交通方式
	 */
	public String getTransportMode() {
		return transportMode;
	}
	/**
	 * 设置：行程天数
	 */
	public void setDays(Integer days) {
		this.days = days;
	}
	/**
	 * 获取：行程天数
	 */
	public Integer getDays() {
		return days;
	}
	/**
	 * 设置：线路详情
	 */
	public void setRouteDetail(String routeDetail) {
		this.routeDetail = routeDetail;
	}
	/**
	 * 获取：线路详情
	 */
	public String getRouteDetail() {
		return routeDetail;
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
	 * 设置：导游姓名
	 */
	public void setGuideName(String guideName) {
		this.guideName = guideName;
	}
	/**
	 * 获取：导游姓名
	 */
	public String getGuideName() {
		return guideName;
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
	 * 设置：导游介绍
	 */
	public void setGuideResume(String guideResume) {
		this.guideResume = guideResume;
	}
	/**
	 * 获取：导游介绍
	 */
	public String getGuideResume() {
		return guideResume;
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

}
