package com.zhiyouxing.travel.entity.vo;

import java.io.Serializable;

/**
 * 实名认证信息（跨服务只读投影）。
 *
 * user_identity 归 user 服务写，travel 服务只在这一处读它：保存报团单前
 * 确认下单人已实名且**审核通过**，并把姓名（以及证件号，供将来核对）抄进订单。
 * 两个服务连的是同一个库，和 attraction / hotel 的 RealNameDao 读同一张表
 * 是同一类取舍 —— 多一张表的耦合，省掉一次服务间调用。
 *
 * 与 attraction / hotel 的同名类同源，是同一套字段的第三份拷贝
 * （为什么不上提到 common，见 com.zhiyouxing.travel.dao.RealNameDao 的类注释）。
 */
public class RealNameInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String APPROVED = "已通过";
    private static final String REJECTED = "已驳回";

    private String realName;

    private String idCard;

    /** 待审核 / 已通过 / 已驳回 */
    private String auditStatus;

    /** 驳回原因，会拼进给游客的提示里 */
    private String auditReply;

    /**
     * 不能下单的原因；审核已通过返回 null。
     *
     * 列值为空也按「待审核」挡住而不是放行 —— 这是下单闸门，
     * 拿不准的时候宁可多挡一次，别漏放一单。
     */
    public String auditBlockReason() {
        if (APPROVED.equals(auditStatus)) {
            return null;
        }
        if (REJECTED.equals(auditStatus)) {
            String reason = auditReply == null || auditReply.isBlank() ? "请修改后重新提交" : auditReply;
            return "实名认证未通过：" + reason;
        }
        return "实名认证正在审核中，通过后即可下单";
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
}
