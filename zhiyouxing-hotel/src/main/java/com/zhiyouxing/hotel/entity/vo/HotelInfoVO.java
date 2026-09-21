package com.zhiyouxing.hotel.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 酒店信息（酒店级，客房见 RoomTypeVO）
 */
public class HotelInfoVO  implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	private Long id;

	/**
	 * 酒店名称
	 */
	private String hotelName;

	/**
	 * 酒店地址
	 */
	private String hotelAddress;

	/**
	 * 酒店图片
	 */
	private String hotelImage;

	/**
	 * 酒店介绍
	 */
	private String hotelIntro;

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
	 * 审核状态：待审核 / 已通过 / 已驳回
	 */
	private String auditStatus;

	/**
	 * 审核回复
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


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	 * 设置：酒店地址
	 */
	public void setHotelAddress(String hotelAddress) {
		this.hotelAddress = hotelAddress;
	}

	/**
	 * 获取：酒店地址
	 */
	public String getHotelAddress() {
		return hotelAddress;
	}

	/**
	 * 设置：酒店图片
	 */
	public void setHotelImage(String hotelImage) {
		this.hotelImage = hotelImage;
	}

	/**
	 * 获取：酒店图片
	 */
	public String getHotelImage() {
		return hotelImage;
	}

	/**
	 * 设置：酒店介绍
	 */
	public void setHotelIntro(String hotelIntro) {
		this.hotelIntro = hotelIntro;
	}

	/**
	 * 获取：酒店介绍
	 */
	public String getHotelIntro() {
		return hotelIntro;
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

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

}
