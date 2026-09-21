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
 * 旅游攻略
 */
@TableName("travel_guide")
public class TravelGuideEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public TravelGuideEntity() {
		
	}
	
	public TravelGuideEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 攻略标题
	 */
					
	private String guideTitle;
	
	/**
	 * 攻略详情
	 */
					
	private String guideDetail;
	
	/**
	 * 用户账号
	 */

	private String userAccount;

	/**
	 * 作者昵称。
	 *
	 * 写入时由服务端从 user 表解析后冗余落库（见 TravelGuideController.createGuide），
	 * 页面上的作者一栏显示的是它而不是 user_account（后者是手机号）。
	 * 老数据没有值时为 null，页面上回落显示账号。
	 */
	private String userName;

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
	 * 设置：攻略标题
	 */
	public void setGuideTitle(String guideTitle) {
		this.guideTitle = guideTitle;
	}
	/**
	 * 获取：攻略标题
	 */
	public String getGuideTitle() {
		return guideTitle;
	}
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
	 * 设置：作者昵称
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 获取：作者昵称
	 */
	public String getUserName() {
		return userName;
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
