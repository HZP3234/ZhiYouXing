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
 * 客房（一家酒店在售的房型）
 *
 * <p>旧结构里 room_type 只是一份全局房型字典（房型名 + 图片），房型数据混在
 * hotel_info 里；现在两者对调：hotel_info 是酒店，这张表就是客房本身。
 * hotel_name 是归属列（客房属于哪家酒店），由酒店前台按登录员工
 * hotel_staff.hotel_name 带出，不接受前端传值。
 *
 * <p>客房由酒店前台自己添加，**不需要审核**（需要审核的是酒店本身）。
 */
@TableName("room_type")
public class RoomTypeEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public RoomTypeEntity() {

	}

	public RoomTypeEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}

	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;

	/**
	 * 所属酒店名称（归属列）
	 */
	private String hotelName;

	/**
	 * 客房名称
	 */
	private String roomName;

	/**
	 * 客房类型（大床房 / 双床房 / ...）
	 */
	private String roomType;

	/**
	 * 客房图片
	 */
	private String roomImage;

	/**
	 * 客房设施
	 */
	private String roomFacility;

	/**
	 * 客房价格
	 */
	private Integer roomPrice;

	/**
	 * 客房数量（剩余可订间数）
	 */
	private Integer roomCount;

	/**
	 * 客房介绍
	 */
	private String roomDescription;


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
	 * 设置：所属酒店名称
	 */
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}
	/**
	 * 获取：所属酒店名称
	 */
	public String getHotelName() {
		return hotelName;
	}
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
	 * 设置：客房类型
	 */
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}
	/**
	 * 获取：客房类型
	 */
	public String getRoomType() {
		return roomType;
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
	 * 设置：客房设施
	 */
	public void setRoomFacility(String roomFacility) {
		this.roomFacility = roomFacility;
	}
	/**
	 * 获取：客房设施
	 */
	public String getRoomFacility() {
		return roomFacility;
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
	 * 设置：客房介绍
	 */
	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}
	/**
	 * 获取：客房介绍
	 */
	public String getRoomDescription() {
		return roomDescription;
	}

}
