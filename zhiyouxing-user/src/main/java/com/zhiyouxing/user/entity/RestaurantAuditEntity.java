package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 餐厅审核台读写的那个视图（表名 restaurant，实体在 zhiyouxing-food 里）。
 *
 * <p>与 AttractionAuditEntity 同一个理由：管理端页面的请求前缀固定是管理员自己的表名
 * （AdminCrud.adminPrefix() → users），而 food 服务里没有 /users 这一层，
 * 所以审核接口必须带着一份裁剪副本挂在 /users/** 下，读的是同一个 zhiyouxing 库里的表。
 *
 * <p><b>别与 restaurant_reservation 搞混</b>：那张表上也有 audit_status / audit_reply，
 * 是<b>餐厅前台审「订座」</b>用的（见 RestaurantReservationEntity 与前端
 * adminResources.js 的 restaurant_reservation，kind 也是 audit）。这里审的是
 * <b>餐厅主表的内容</b>（店名/地址/菜品…），审核人是<b>管理员</b>，两者同名列不同表。
 *
 * <p>列是 restaurant 那份实体减掉六个计数（赞/踩/点击次数/最近点击/评论/收藏）。
 */
@TableName("restaurant")
public class RestaurantAuditEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /** 餐厅名称 */
    private String restaurantName;

    /** 餐厅图片路径，多个用英文逗号分隔 */
    private String restaurantImage;

    /** 特色菜品 */
    private String signatureDish;

    /** 营业时间 */
    private String businessHours;

    /**
     * 人均消费。库里这列是 varchar —— 存的是「人均消费1」这种文案而非数字，
     * 所以别顺手改成 Double，与 RestaurantEntity / RestaurantView 保持一致。
     */
    private String avgCost;

    /** 餐厅地址 */
    private String restaurantAddress;

    /** 联系电话 */
    private String contactPhone;

    /** 餐厅简介 */
    private String restaurantIntro;

    /** 待审核 / 已通过 / 已驳回。餐厅前台一经手就重置成待审核 */
    private String auditStatus;

    /** 审核回复。驳回时是原因，会回显到餐厅前台的餐厅信息页 */
    private String auditReply;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date auditTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public String getSignatureDish() {
        return signatureDish;
    }

    public void setSignatureDish(String signatureDish) {
        this.signatureDish = signatureDish;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(String avgCost) {
        this.avgCost = avgCost;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getRestaurantIntro() {
        return restaurantIntro;
    }

    public void setRestaurantIntro(String restaurantIntro) {
        this.restaurantIntro = restaurantIntro;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditReply() {
        return auditReply;
    }

    public void setAuditReply(String auditReply) {
        this.auditReply = auditReply;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }
}
