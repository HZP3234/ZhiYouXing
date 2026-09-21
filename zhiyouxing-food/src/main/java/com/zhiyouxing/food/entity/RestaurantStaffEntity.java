package com.zhiyouxing.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 餐厅前台
 */
@TableName("restaurant_staff")
public class RestaurantStaffEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 登录账号（手机号）
	 */
	private String staffAccount;

	/**
	 * 密码
	 *
	 * 只反序列化、不序列化：注册/登录要能收密码，但 /session、/info 不该把它回给前端。
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	/**
	 * 姓名
	 */
	private String staffName;

	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 联系方式
	 */
	private String contactPhone;

	/**
	 * 所属餐厅名称
	 */
	private String restaurantName;

	/**
	 * 所属餐厅地址
	 */
	private String restaurantAddress;

	/**
	 * 状态 0正常 1锁定
	 */
	private Integer status;

	/**
	 * 密码错误次数
	 */
	private Integer passwordWrongNum;

	@JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date addTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStaffAccount() {
		return staffAccount;
	}

	public void setStaffAccount(String staffAccount) {
		this.staffAccount = staffAccount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStaffName() {
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public String getRestaurantAddress() {
		return restaurantAddress;
	}

	public void setRestaurantAddress(String restaurantAddress) {
		this.restaurantAddress = restaurantAddress;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getPasswordWrongNum() {
		return passwordWrongNum;
	}

	public void setPasswordWrongNum(Integer passwordWrongNum) {
		this.passwordWrongNum = passwordWrongNum;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
}
