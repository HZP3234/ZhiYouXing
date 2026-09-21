package com.zhiyouxing.user.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhiyouxing.common.utils.IdCardUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.dao.UserIdentityDao;
import com.zhiyouxing.user.entity.UserIdentityEntity;
import com.zhiyouxing.user.entity.vo.UserIdentityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 实名认证（游客侧）。
 *
 * 这里是「提交」，审核在管理端的 /users/user_identity（见 UsersScopeController）。
 * 提交完 status 是待审核，attraction / hotel / travel 三个服务下单前查这张表，
 * 不是「已通过」一律拒绝。
 *
 * 校验放在服务端而不是只靠前端 —— 前端那份是为了即时提示，
 * 校验位、出生日期、证件号唯一性都得有一份绕不过去的实现。
 */
@RestController
@RequestMapping("/user/identity")
public class IdentityController {

    @Autowired
    private UserIdentityDao userIdentityDao;

    /**
     * 当前登录用户的认证状态。购票页进页面时调它，决定渲染认证表单还是渲染已认证信息。
     */
    @GetMapping("/detail")
    public R detail(HttpServletRequest request) {
        String account = currentAccount(request);
        if (account == null) {
            return R.error(401, "请先登录");
        }
        UserIdentityEntity row = findByAccount(account);
        return R.ok().put("data", row == null ? UserIdentityVO.notVerified() : UserIdentityVO.of(row));
    }

    /**
     * 提交认证。前端只传 realName / idCard，归属从登录态取 ——
     * 请求体里带 userAccount 也没用，这里不看它（否则能给别人的账号做认证）。
     *
     * 提交只是「入库等审核」：audit_status 写待审核、verify_time 留空，
     * 能不能下单由管理端审过之后说了算（见 UsersScopeController 与三个下单接口）。
     */
    @PostMapping("/save")
    public R save(@RequestBody UserIdentityEntity form, HttpServletRequest request) {
        String account = currentAccount(request);
        Long userId = currentUserId(request);
        if (account == null || userId == null) {
            return R.error(401, "请先登录");
        }

        String realName = StrUtil.trim(form.getRealName());
        String idCard = IdCardUtils.normalize(form.getIdCard());

        if (!IdCardUtils.isValidRealName(realName)) {
            return R.error("姓名不合法：请填 2~15 位汉字或 2~30 位字母");
        }
        if (!IdCardUtils.isValid(idCard)) {
            return R.error("身份证号不正确，请核对 18 位号码与最后一位校验码");
        }

        UserIdentityEntity exists = findByAccount(account);
        if (exists != null && UserIdentityVO.APPROVED.equals(exists.getAuditStatus())) {
            /*
             * 已经审过了。同号重复提交按成功返回（用户连点两次不该看到红字），
             * 换号则明确拒绝：认证记录是订单（ticket_order.id_card）的凭据，
             * 允许自行改号等于让已开出的门票失去实名意义。
             */
            if (!exists.getIdCard().equals(idCard)) {
                return R.error("本账号已完成实名认证，证件号不能自行修改，如需变更请联系客服");
            }
            return R.ok().put("data", UserIdentityVO.of(exists));
        }

        /*
         * 证件号唯一性：新提交要保证没被别人占用；重新提交时要把自己那条排掉，
         * 否则「驳回后原样再交一次」会被自己挡住。
         */
        QueryWrapper<UserIdentityEntity> usedEw = new QueryWrapper<UserIdentityEntity>()
                .eq("id_card", idCard);
        if (exists != null) {
            usedEw.ne("id", exists.getId());
        }
        Long used = userIdentityDao.selectCount(usedEw);
        if (used != null && used > 0) {
            return R.error("该证件号已被其他账号认证，一个证件号只能认证一个账号");
        }

        UserIdentityEntity row = new UserIdentityEntity();
        row.setUserId(userId);
        row.setUserAccount(account);
        row.setRealName(realName);
        row.setIdCard(idCard);
        row.setAuditStatus(UserIdentityVO.PENDING);

        if (exists == null) {
            userIdentityDao.insert(row);
            return R.ok().put("data", UserIdentityVO.of(row));
        }

        /*
         * 重新提交（上一轮被驳回）走 UPDATE，且必须用 UpdateWrapper 显式 set：
         * updateById 会跳过实体里的 null 字段，清不掉上一轮的通过时间与驳回原因，
         * 结果是「重新提交了但列表里还挂着上次的驳回理由」。
         */
        row.setId(exists.getId());
        userIdentityDao.update(null, new UpdateWrapper<UserIdentityEntity>()
                .eq("id", exists.getId())
                .set("user_id", userId)
                .set("real_name", realName)
                .set("id_card", idCard)
                .set("verify_time", null)
                .set("audit_status", UserIdentityVO.PENDING)
                .set("audit_reply", null)
                .set("audit_time", null));

        return R.ok().put("data", UserIdentityVO.of(row));
    }

    private UserIdentityEntity findByAccount(String account) {
        return userIdentityDao.selectOne(
                new QueryWrapper<UserIdentityEntity>().eq("user_account", account));
    }

    private String currentAccount(HttpServletRequest request) {
        String account = (String) request.getSession().getAttribute("username");
        return StrUtil.isBlank(account) ? null : account;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute("user_id");
    }
}
