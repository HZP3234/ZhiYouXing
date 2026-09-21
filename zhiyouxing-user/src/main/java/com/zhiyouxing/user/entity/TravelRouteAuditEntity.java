package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 线路审核台读写的那个视图（表名 travel_route，实体在 zhiyouxing-travel 里）。
 *
 * <p>为什么在 user 服务里另建一份：管理端页面的请求前缀固定是管理员自己的表名
 * （AdminCrud.adminPrefix() → users），审核接口必须挂在 /users/** 下才点得到，
 * 而 travel 服务里没有 /users 这一层。两个服务连的是同一个 zhiyouxing 库，
 * 跨服务读同一张表这里已有先例（ConsumptionController 读 ticket_order、
 * 三个下单服务读 user_identity）。
 *
 * <p>列是 travel 那份实体减掉用不上的：景点三件套（精简后的表单不再填）、
 * 赞踩评论收藏四个计数（跟审核无关，而且审核台只读不写展示字段）。
 * 管理员本来就只能改 audit_status / audit_reply / audit_time 三列，
 * 少了这些列反而让「改不动别的字段」在类型上就成立。
 */
@TableName("travel_route")
public class TravelRouteAuditEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /** 线路名称 */
    private String routeName;

    /** 线路图片路径，多个用英文逗号分隔（与其它表的 images 字段同一约定） */
    private String routeImage;

    private String startPoint;

    private String endPoint;

    private Integer days;

    private Double routeFee;

    /** 报团名额 */
    private Integer groupQuota;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date departureDate;

    private String transportMode;

    /** 线路详情 */
    private String routeDetail;

    /** 提交线路的导游工号（旅游线路与导游的归属列） */
    private String guideNo;

    private String guideName;

    private String contactPhone;

    /** 待审核 / 已通过 / 已驳回。导游一经手就重置成待审核 */
    private String auditStatus;

    /** 审核回复。驳回时是原因，会回显到导游的线路列表 */
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

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteImage() {
        return routeImage;
    }

    public void setRouteImage(String routeImage) {
        this.routeImage = routeImage;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Double getRouteFee() {
        return routeFee;
    }

    public void setRouteFee(Double routeFee) {
        this.routeFee = routeFee;
    }

    public Integer getGroupQuota() {
        return groupQuota;
    }

    public void setGroupQuota(Integer groupQuota) {
        this.groupQuota = groupQuota;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getRouteDetail() {
        return routeDetail;
    }

    public void setRouteDetail(String routeDetail) {
        this.routeDetail = routeDetail;
    }

    public String getGuideNo() {
        return guideNo;
    }

    public void setGuideNo(String guideNo) {
        this.guideNo = guideNo;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
