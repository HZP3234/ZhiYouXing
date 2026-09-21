package com.zhiyouxing.travel.entity.model;

import java.io.Serializable;
 

/**
 * 旅游攻略
 */
public class TravelGuideModel  implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 攻略详情
	 */
	
	private String guideDetail;
		
	/**
	 * 用户账号
	 */
	
	private String userAccount;
		
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
	 * 设置：攻略详情
	 */
	 
	public void setGuideDetail(String guideDetail) {
		this.guideDetail = guideDetail;
	}
	
	/**
	 * 获取：攻略详情
	 */
	public String getGuideDetail() {
		return guideDetail;
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
			
}
