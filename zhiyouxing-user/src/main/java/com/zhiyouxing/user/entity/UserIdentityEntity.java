package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实名认证（表名 user_identity）。
 *
 * 与 user 表分开存：证件号是敏感字段，而 /user/session、/user/list 返回的是整条
 * user 记录，放在那张表里等于把证件号带进每一个响应。
 *
 * user_account 与 id_card 在库里都是 UNIQUE —— 一个账号一条记录（重认证走 UPDATE），
 * 一个证件号只能认证一个账号（插重了数据库会直接报唯一键冲突，Controller 里先查再插，
 * 避免把 500 抛给用户）。
 *
 * 审核三列（audit_*）由管理端写：提交时是「待审核」且 verify_time 留空，管理员通过才写
 * verify_time。所以 verify_time 的语义是「审核通过时间」，不再是「提交时间」。
 */
@TableName("user_identity")
public class UserIdentityEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /** 认证所属用户，取自登录态（user.id） */
    private Long userId;

    /** 认证所属账号，取自登录态（user.user_account），不收前端传值 */
    private String userAccount;

    private String realName;

    /** 完整证件号。只进库，不出接口 —— 对外一律走 IdCardUtils.mask */
    private String idCard;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date verifyTime;

    /** 审核状态：待审核 / 已通过 / 已驳回。只有「已通过」才解锁下单 */
    private String auditStatus;

    /** 审核回复。驳回时是原因（回显到购票页），通过时是一句结论 */
    private String auditReply;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date auditTime;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Date getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime) {
        this.verifyTime = verifyTime;
    }
}
