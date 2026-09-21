package com.zhiyouxing.hotel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 酒店信息（酒店级，不是房型）
 *
 * <p>客房在 room_type 表里（RoomTypeEntity）。这张表只放酒店自己的字段，
 * 以及审核三列：酒店前台提交后服务端把 audit_status 盖成「待审核」，
 * 管理员在 /users/hotel_info_audit 通过后才「营业」——游客端 /hotel_info/list
 * 与 /detail/{id} 只放行「已通过」的酒店。
 */
@TableName("hotel_info")
public class HotelInfoEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public HotelInfoEntity() {

	}

	public HotelInfoEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}

	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
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
	 * 审核状态：待审核 / 已通过 / 已驳回（仅「已通过」游客端可见、可订）
	 */
	private String auditStatus;

	/**
	 * 审核回复（驳回原因，酒店前台在管理端能看到）
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

}
