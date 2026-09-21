package com.zhiyouxing.user.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhiyouxing.common.utils.IdCardUtils;
import com.zhiyouxing.user.entity.UserIdentityEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 实名认证的管理端形态（管理端「实名认证」页的列表与详情）。
 *
 * 与游客侧的 UserIdentityVO 分开：那边回答的是「我认证了吗」，只需要 verified 一个结论；
 * 这边要的是「这条记录是谁提交的、什么时候提交的、审到哪一步了」，
 * 所以带上 id / user_account / add_time，前端通用列表页靠 id 定位行。
 *
 * 证件号在这里同样是脱敏的 —— 库里的完整号码不出接口这条规矩不分端，
 * 管理端审核看的是姓名与号码段对不对得上，不需要完整号（真要看完整号，
 * 那属于「审计日志」级别的需求，应该另开一个带操作留痕的接口，而不是放开这个 VO）。
 */
public class IdentityAuditVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date addTime;

    /** 认证所属账号（user.user_account） */
    private String userAccount;

    private String realName;

    /** 脱敏证件号（前 6 后 4） */
    private String idCardMasked;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifyTime;

    private String auditStatus;

    private String auditReply;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    public static IdentityAuditVO of(UserIdentityEntity row) {
        IdentityAuditVO vo = new IdentityAuditVO();
        vo.id = row.getId();
        vo.addTime = row.getAddTime();
        vo.userAccount = row.getUserAccount();
        vo.realName = row.getRealName();
        vo.idCardMasked = IdCardUtils.mask(row.getIdCard());
        vo.verifyTime = row.getVerifyTime();
        vo.auditStatus = row.getAuditStatus();
        vo.auditReply = row.getAuditReply();
        vo.auditTime = row.getAuditTime();
        return vo;
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

    public String getIdCardMasked() {
        return idCardMasked;
    }

    public void setIdCardMasked(String idCardMasked) {
        this.idCardMasked = idCardMasked;
    }

    public Date getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime) {
        this.verifyTime = verifyTime;
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
