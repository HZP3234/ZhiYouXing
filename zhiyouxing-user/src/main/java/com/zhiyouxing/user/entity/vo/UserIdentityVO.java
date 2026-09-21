package com.zhiyouxing.user.entity.vo;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhiyouxing.common.utils.IdCardUtils;
import com.zhiyouxing.user.entity.UserIdentityEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 实名认证状态（对前端）。
 *
 * 认证接口一律返回这个对象，而不是实体本身 —— 实体里是完整的证件号，
 * 直接用实体当返回值等于把证件号发回浏览器。这里只带脱敏后的号。
 *
 * 未认证时同样返回 200 和这个对象（verified=false、auditStatus=""），
 * 不用 404 或错误码：「还没认证」是购票页的正常状态，前端要拿它决定渲染表单
 * 还是渲染认证信息。
 *
 * verified 只认「已通过」：提交完还没人审的时候是 false，
 * 前端要按 auditStatus 区分「没提交过」（空）和「提交了在等审核」「被打回了」。
 */
public class UserIdentityVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 审核状态常量，与库里 varchar 取值一致 */
    public static final String PENDING = "待审核";
    public static final String APPROVED = "已通过";
    public static final String REJECTED = "已驳回";

    /** 是否已通过实名认证（只有「已通过」为 true） */
    private boolean verified;

    /** 真实姓名。未认证时为 null */
    private String realName;

    /** 脱敏证件号（前 6 后 4）。完整号码不出接口 */
    private String idCardMasked;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifyTime;

    /** 待审核 / 已通过 / 已驳回；没提交过是空串（不是 null，前端少判一次） */
    private String auditStatus = "";

    /** 审核回复。驳回时是原因，前端会把它显示在表单上方 */
    private String auditReply;

    public static UserIdentityVO notVerified() {
        return new UserIdentityVO();
    }

    public static UserIdentityVO of(UserIdentityEntity row) {
        UserIdentityVO vo = new UserIdentityVO();
        vo.auditStatus = StrUtil.blankToDefault(row.getAuditStatus(), PENDING);
        vo.verified = APPROVED.equals(vo.auditStatus);
        vo.realName = row.getRealName();
        vo.idCardMasked = IdCardUtils.mask(row.getIdCard());
        vo.verifyTime = row.getVerifyTime();
        vo.auditReply = row.getAuditReply();
        return vo;
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
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
}
