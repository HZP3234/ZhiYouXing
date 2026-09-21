package com.zhiyouxing.travel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 导游
 */
@TableName("tour_guide")
public class TourGuideEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public TourGuideEntity() {

	}

	public TourGuideEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}

	/**
	 * 主键id
	 */
	@TableId
	private Long id;
	/**
	 * 导游工号
	 */

	private String guideNo;

	/**
	 * 密码
	 */

	private String password;

	/**
	 * 导游姓名
	 */

	private String guideName;

	/**
	 * 头像
	 */

	private String avatar;

	/**
	 * 专业领域
	 */

	private String specialty;

	/**
	 * 语言能力
	 */

	private String languageSkill;

	/**
	 * 联系方式
	 */

	private String contactPhone;

	/**
	 * 个人履历
	 */

	private String guideResume;

	/**
	 * 状态
	 */

	private Integer status;

	/**
	 * 密码错误次数
	 */

	private Integer passwordWrongNum;


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
	 * 设置：密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * 获取：密码
	 */
	public String getPassword() {
		return password;
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
	 * 设置：头像
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	/**
	 * 获取：头像
	 */
	public String getAvatar() {
		return avatar;
	}
	/**
	 * 设置：专业领域
	 */
	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}
	/**
	 * 获取：专业领域
	 */
	public String getSpecialty() {
		return specialty;
	}
	/**
	 * 设置：语言能力
	 */
	public void setLanguageSkill(String languageSkill) {
		this.languageSkill = languageSkill;
	}
	/**
	 * 获取：语言能力
	 */
	public String getLanguageSkill() {
		return languageSkill;
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
	 * 设置：个人履历
	 */
	public void setGuideResume(String guideResume) {
		this.guideResume = guideResume;
	}
	/**
	 * 获取：个人履历
	 */
	public String getGuideResume() {
		return guideResume;
	}
	/**
	 * 设置：状态
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 获取：状态
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * 设置：密码错误次数
	 */
	public void setPasswordWrongNum(Integer passwordWrongNum) {
		this.passwordWrongNum = passwordWrongNum;
	}
	/**
	 * 获取：密码错误次数
	 */
	public Integer getPasswordWrongNum() {
		return passwordWrongNum;
	}

}
