package com.zhiyouxing.hotel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 酒店预订
 */
@TableName("hotel_reservation")
public class HotelReservationEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public HotelReservationEntity() {
		
	}
	
	public HotelReservationEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 预订单号
	 */
					
	private String reservationNo;
	
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
	 * 实名认证身份证号。
	 *
	 * @JsonIgnore：这一列只进不出。 /hotel_reservation/detail/{id} 与 /list 都是
	 * @IgnoreAuth 的公开接口、又不按账号过滤，不加这个注解，任何人猜个 id 就能读到
	 * 别人的证件号（管理端要看完整号码请直接查库）。注解加在字段上，子类
	 * HotelReservationView 一并生效。顺带也挡住反序列化：请求体里塞 idCard 会被忽略，
	 * 随后由 HotelReservationController.createOrder 用服务端查到的实名信息覆盖。
	 */
	@JsonIgnore
	private String idCard;

	/**
	 * 是否支付
	 */

	private String isPay;
	
	
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
	 * 设置：预订单号
	 */
	public void setReservationNo(String reservationNo) {
		this.reservationNo = reservationNo;
	}
	/**
	 * 获取：预订单号
	 */
	public String getReservationNo() {
		return reservationNo;
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
	 * 设置：实名认证身份证号
	 */
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	/**
	 * 获取：实名认证身份证号
	 */
	public String getIdCard() {
		return idCard;
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
