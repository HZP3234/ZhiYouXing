package com.zhiyouxing.attraction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 景点类型
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("attraction_type")
public class AttractionTypeEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public AttractionTypeEntity() {
		
	}
	
	public AttractionTypeEntity(T t) {
		BeanUtils.copyProperties(t, this);
	}
	
	/**
	 * 主键id
	 */
    @TableId(type = IdType.AUTO)
    private Long id;
	/**
	 * 景点类型
	 */
					
	private String attractionType;
	
	/**
	 * 图片
	 */
					
	private String image;
	
	
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
	 * 设置：景点类型
	 */
	public void setAttractionType(String attractionType) {
		this.attractionType = attractionType;
	}
	/**
	 * 获取：景点类型
	 */
	public String getAttractionType() {
		return attractionType;
	}
	/**
	 * 设置：图片
	 */
	public void setImage(String image) {
		this.image = image;
	}
	/**
	 * 获取：图片
	 */
	public String getImage() {
		return image;
	}

}
