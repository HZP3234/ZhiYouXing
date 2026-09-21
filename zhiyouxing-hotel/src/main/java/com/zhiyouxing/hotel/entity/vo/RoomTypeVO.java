package com.zhiyouxing.hotel.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 客房（酒店前台添加，无需审核）
 */
public class RoomTypeVO  implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	private Long id;

	/**
	 * 所属酒店名称
	 */
	private String hotelName;

	/**
	 * 客房名称
	 */
	private String roomName;

	/**
	 * 客房类型
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
	 * 客房数量
	 */
	private Integer roomCount;

	/**
	 * 客房介绍
	 */
	private String roomDescription;

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

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

}
