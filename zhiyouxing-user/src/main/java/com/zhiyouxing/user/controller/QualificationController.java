package com.zhiyouxing.user.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.dao.QualificationDao;
import com.zhiyouxing.user.entity.QualificationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * 管理端资质的提交侧（四个管理端角色各自的「个人资料」页在用）。
 *
 * 审核侧在 /users/qualification（见 UsersScopeController）—— 审核接口是管理员专用的，
 * 提交接口是四个角色共用的，两者的鉴权口径完全不同，所以不放在同一个类里。
 *
 * 归属（账号 / 角色 / 所在表）全部从登录态取，请求体里传这些字段一律不看：
 * 否则任何一个登录的人都能替别的角色交一份资质上去。
 */
@RestController
@RequestMapping("/user/qualification")
public class QualificationController {

    /**
     * 有资质要交的角色，比的是登录态里的 table_name（网关 token 里记的那个）。
     * 导游这里是 tour_guide 而不是前端角色键 daoyou —— 后端到处都用真表名
     * （见 TourGuideController.login 的 generateToken、ConsultController.TABLE_GUIDE）。
     */
    private static final Set<String> SUBMITTERS =
            Set.of("tour_guide", "hotel_staff", "attraction_staff", "restaurant_staff");

    @Autowired
    private QualificationDao qualificationDao;

    /**
     * 当前登录管理端账号的资质。没提交过返回 data=null，
     * 前端据此渲染「还没提交」的表单（与实名认证返回 verified=false 的对象不同：
     * 那边有 verified 这个语义位要表达，这边没有记录就是没有）。
     */
    @GetMapping("/detail")
    public R detail(HttpServletRequest request) {
        String tableName = currentTable(request);
        if (!SUBMITTERS.contains(tableName)) {
            return R.error("当前角色无需提交资质");
        }
        return R.ok().put("data", findByAccount(currentAccount(request)));
    }

    /**
     * 提交 / 重新提交资质。两个动作同一个入口：
     * 有记录就改那一条并把审核状态重置回待审核（材料变了就得重审），没有就插一条。
     */
    @PostMapping("/save")
    public R save(@RequestBody QualificationEntity form, HttpServletRequest request) {
        String tableName = currentTable(request);
        String account = currentAccount(request);
        Long userId = currentUserId(request);
        if (account == null || userId == null) {
            return R.error(401, "请先登录");
        }
        if (!SUBMITTERS.contains(tableName)) {
            return R.error("当前角色无需提交资质");
        }

        String realName = StrUtil.trim(form.getRealName());
        String contactPhone = StrUtil.trim(form.getContactPhone());
        String orgName = StrUtil.trim(form.getOrgName());
        String certNo = StrUtil.trim(form.getCertNo());
        if (StrUtil.isBlank(realName)) {
            return R.error("请填写真实姓名");
        }
        if (StrUtil.isBlank(orgName)) {
            return R.error("请填写所属单位");
        }
        if (StrUtil.isBlank(certNo)) {
            return R.error("请填写资质证书编号");
        }

        QualificationEntity exists = findByAccount(account);
        // 角色名取登录态而不是请求体：改 role 字段就能伪装成别的角色，这条不该由前端决定
        String roleName = currentRole(request);

        if (exists == null) {
            QualificationEntity row = new QualificationEntity();
            row.setUserId(userId);
            row.setUserAccount(account);
            row.setTableName(tableName);
            row.setRoleName(roleName);
            row.setRealName(realName);
            row.setContactPhone(contactPhone);
            row.setOrgName(orgName);
            row.setCertNo(certNo);
            row.setQualification(form.getQualification());
            row.setImage(form.getImage());
            row.setAuditStatus("待审核");
            qualificationDao.insert(row);
            return R.ok().put("data", row);
        }

        /*
         * 重新提交走 UpdateWrapper 显式 set：updateById 跳过 null 字段，
         * 清不掉上一轮的审核结论，会出现「重新交了材料，状态却还是已驳回/已通过」。
         */
        qualificationDao.update(null, new UpdateWrapper<QualificationEntity>()
                .eq("id", exists.getId())
                .set("user_id", userId)
                .set("table_name", tableName)
                .set("role_name", roleName)
                .set("real_name", realName)
                .set("contact_phone", contactPhone)
                .set("org_name", orgName)
                .set("cert_no", certNo)
                .set("qualification", form.getQualification())
                .set("image", form.getImage())
                .set("audit_status", "待审核")
                .set("audit_reply", null)
                .set("audit_time", null));

        return R.ok().put("data", findByAccount(account));
    }

    private QualificationEntity findByAccount(String account) {
        return qualificationDao.selectOne(
                new QueryWrapper<QualificationEntity>().eq("user_account", account));
    }

    private String currentAccount(HttpServletRequest request) {
        String account = (String) request.getSession().getAttribute("username");
        return StrUtil.isBlank(account) ? null : account;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute("user_id");
    }

    private String currentTable(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("table_name");
    }

    private String currentRole(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("role");
    }
}
