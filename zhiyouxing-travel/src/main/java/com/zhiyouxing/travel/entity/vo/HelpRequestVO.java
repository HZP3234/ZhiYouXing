package com.zhiyouxing.travel.entity.vo;

import java.io.Serializable;
 

/**
 * 求救信息
 */
public class HelpRequestVO implements Serializable {
	private static final long serialVersionUID = 1L;

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

	public void setGuideNo(String guideNo) {
		this.guideNo = guideNo;
	}
	public String getGuideNo() {
		return guideNo;
	}

	public void setGuideName(String guideName) {
		this.guideName = guideName;
	}
	public String getGuideName() {
		return guideName;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getUserAccount() {
		return userAccount;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserName() {
		return userName;
	}

	public void setHelpContent(String helpContent) {
		this.helpContent = helpContent;
	}
	public String getHelpContent() {
		return helpContent;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getContactPhone() {
		return contactPhone;
	}

	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}
	public String getAuditStatus() {
		return auditStatus;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}
	public String getReplyContent() {
		return replyContent;
	}
			
}
