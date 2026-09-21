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
 * 求救信息
 */
@TableName("help_request")
public class HelpRequestEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public HelpRequestEntity() {
		
	}
	
	public HelpRequestEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;

	/**
	 * 导游工号
	 */
	private String guideNo;
	
	/**
	 * 导游姓名
	 */
	private String guideName;
	
	/**
	 * 用户账号
	 */
	private String userAccount;
	
	/**
	 * 用户姓名
	 */
	private String userName;
	
	/**
	 * 求救内容
	 */
	private String helpContent;
	
	/**
	 * 联系方式
	 */
	private String contactPhone;
	
	/**
	 * 是否处理
	 */
	private String auditStatus;
	
	/**
	 * 回复内容
	 */
	private String replyContent;
	
	
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
	 * 设置：求救内容
	 */
	public void setHelpContent(String helpContent) {
		this.helpContent = helpContent;
	}
	/**
	 * 获取：求救内容
	 */
	public String getHelpContent() {
		return helpContent;
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
	 * 设置：是否处理
	 */
	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}
	/**
	 * 获取：是否处理
	 */
	public String getAuditStatus() {
		return auditStatus;
	}

	/**
	 * 设置：回复内容
	 */
	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}
	/**
	 * 获取：回复内容
	 */
	public String getReplyContent() {
		return replyContent;
	}

}
