package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 酒店审核台读写的那个视图（表名 hotel_info，实体在 zhiyouxing-hotel 里）。
 *
 * <p>为什么在 user 服务里另建一份：管理端页面的请求前缀固定是管理员自己的表名
 * （AdminCrud.adminPrefix() → users），审核接口必须挂在 /users/** 下才点得到，
 * 而 hotel 服务里没有 /users 这一层。两个服务连的是同一个 zhiyouxing 库，
 * 跨服务读同一张表这里已有先例（TravelRouteAuditEntity 就是同一个写法）。
 *
 * <p>列是 hotel 那份实体减掉用不上的四个计数（点击/评论/收藏，跟审核无关，
 * 而且审核台只读不写展示字段）。管理员本来就只能改
 * audit_status / audit_reply / audit_time 三列，少了这些列反而让
 * 「改不动别的字段」在类型上就成立。
 */
@TableName("hotel_info")
public class HotelInfoAuditEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /** 酒店名称 */
    private String hotelName;

    private String hotelAddress;

    /** 酒店图片路径，多个用英文逗号分隔（与其它表的 images 字段同一约定） */
    private String hotelImage;

    /** 酒店介绍 */
    private String hotelIntro;

    /** 待审核 / 已通过 / 已驳回。酒店前台一经手就重置成待审核 */
    private String auditStatus;

    /** 审核回复。驳回时是原因，会回显到酒店前台的酒店信息页 */
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

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public String getHotelImage() {
        return hotelImage;
    }

    public void setHotelImage(String hotelImage) {
        this.hotelImage = hotelImage;
    }

    public String getHotelIntro() {
        return hotelIntro;
    }

    public void setHotelIntro(String hotelIntro) {
        this.hotelIntro = hotelIntro;
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
