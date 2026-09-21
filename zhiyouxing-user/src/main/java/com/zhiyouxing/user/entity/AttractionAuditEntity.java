package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 景点审核台读写的那个视图（表名 attraction，实体在 zhiyouxing-attraction 里）。
 *
 * <p>为什么在 user 服务里另建一份：管理端页面的请求前缀固定是管理员自己的表名
 * （AdminCrud.adminPrefix() → users），审核接口必须挂在 /users/** 下才点得到，
 * 而 attraction 服务里没有 /users 这一层。两个服务连的是同一个 zhiyouxing 库，
 * 跨服务读同一张表这里已有先例（HotelInfoAuditEntity、TravelRouteAuditEntity 都是同一个写法）。
 *
 * <p>列是 attraction 那份实体减掉用不上的五个计数（赞/踩/点击/评论/收藏，跟审核无关，
 * 而且审核台只读不写展示字段）。管理员本来就只能改
 * audit_status / audit_reply / audit_time 三列，少了这些列反而让
 * 「改不动别的字段」在类型上就成立。
 */
@TableName("attraction")
public class AttractionAuditEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /** 景点名称 */
    private String attractionName;

    /** 景点图片路径，多个用英文逗号分隔（与其它表的 images 字段同一约定） */
    private String image;

    /** 景点类型，取值来自 attraction_type 字典 */
    private String attractionType;

    /** 开放时间 */
    private String openingHours;

    /** 门票价格 */
    private Double ticketPrice;

    /** 数量 */
    private Integer quantity;

    /** 景点位置 */
    private String attractionLocation;

    /** 景点介绍 */
    private String attractionDescription;

    /** 待审核 / 已通过 / 已驳回。景点前台一经手就重置成待审核 */
    private String auditStatus;

    /** 审核回复。驳回时是原因，会回显到景点前台的景点信息页 */
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

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAttractionType() {
        return attractionType;
    }

    public void setAttractionType(String attractionType) {
        this.attractionType = attractionType;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(String attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public String getAttractionDescription() {
        return attractionDescription;
    }

    public void setAttractionDescription(String attractionDescription) {
        this.attractionDescription = attractionDescription;
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
