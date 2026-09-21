package com.zhiyouxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理端资质（表名 qualification）。
 *
 * 四个管理端角色（导游 / 酒店前台 / 景点前台 / 餐厅前台）在各自的「个人资料」页提交，
 * 管理员在管理端「资质审核」页审。字段与 user_identity 不共用一张表：
 * 资质是用户自己填的内容（证书编号、从业说明、证书照片），实名认证是身份核验，
 * 两者审核节奏也不同 —— 资质被驳回要重新交材料，实名认证被驳回是改证件信息。
 *
 * table_name 与 user_account 都是从登录态取的，不收前端传值：
 * 前者决定管理端哪一类资质菜单能看到这一行，后者是唯一键（一个账号一条，重交往 UPDATE）。
 */
@TableName("qualification")
public class QualificationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addTime;

    /** 提交人主键（所在角色表的 id） */
    private Long userId;

    /** 提交人账号，取自登录态（token 里的 username），不收前端传值 */
    private String userAccount;

    /** 提交人来自哪个角色：daoyou / hotel_staff / attraction_staff / restaurant_staff */
    private String tableName;

    /** 角色中文名，取自登录态（token 里的 role），管理端列表直接显示 */
    private String roleName;

    private String realName;

    private String contactPhone;

    /** 所属单位（旅行社 / 酒店 / 景点 / 餐厅） */
    private String orgName;

    /** 资质证书编号 */
    private String certNo;

    /** 资质说明（从业年限、执业范围等） */
    private String qualification;

    /** 证书图片路径，多个用英文逗号分隔（与其它表的 images 字段同一约定） */
    private String image;

    /** 待审核 / 已通过 / 已驳回。提交与重新提交都会重置成待审核 */
    private String auditStatus;

    /** 审核回复。驳回时是原因，会回显到提交人的个人资料页 */
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
