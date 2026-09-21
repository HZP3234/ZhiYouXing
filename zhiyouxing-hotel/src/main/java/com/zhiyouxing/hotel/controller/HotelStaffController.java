package com.zhiyouxing.hotel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.interceptor.AuthorizationInterceptor;
import com.zhiyouxing.common.service.TokenService;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.hotel.entity.HotelStaffEntity;
import com.zhiyouxing.hotel.service.HotelStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 酒店前台
 *
 * 只提供管理端自己要用到的登录态与个人资料接口，酒店/客房/预订等业务数据
 * 仍由 HotelInfoController 等既有控制器提供。
 */
@RestController
@RequestMapping("/hotel_staff")
public class HotelStaffController {

    /** 写进 token 的角色名，前端按它区分管理端菜单 */
    private static final String ROLE = "酒店前台";

    @Autowired
    private HotelStaffService hotelStaffService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, HttpServletRequest request) {
        HotelStaffEntity u = hotelStaffService.getOne(
                new QueryWrapper<HotelStaffEntity>().eq("staff_account", username));
        if (u != null && u.getStatus().intValue() == 1) {
            return R.error("账号已锁定，请联系管理员。");
        }
        if (u == null || !u.getPassword().equals(password)) {
            if (u != null) {
                u.setPasswordWrongNum(u.getPasswordWrongNum() + 1);
                if (u.getPasswordWrongNum() >= 3) {
                    u.setStatus(1);
                }
                hotelStaffService.updateById(u);
            }
            return R.error("账号或密码不正确");
        }

        String token = tokenService.generateToken(u.getId(), username, "hotel_staff", ROLE);
        return R.ok().put("token", token);
    }

    /**
     * 注册
     */
    @IgnoreAuth
    @RequestMapping("/register")
    public R register(@RequestBody HotelStaffEntity staff) {
        HotelStaffEntity u = hotelStaffService.getOne(
                new QueryWrapper<HotelStaffEntity>().eq("staff_account", staff.getStaffAccount()));
        if (u != null) {
            return R.error("注册用户已存在");
        }
        /*
         * 同 UserController.register：前台注册只收手机号+密码，姓名不采集。
         * staff_name 可空，但留空会让管理端顶栏显示空白，给个占位名（个人资料里可改）。
         */
        if (StrUtil.isBlank(staff.getStaffName())) {
            staff.setStaffName("酒店前台" + staff.getStaffAccount());
        }
        staff.setId(new Date().getTime());
        hotelStaffService.save(staff);
        return R.ok();
    }

    /**
     * 退出
     */
    @RequestMapping("/logout")
    public R logout(HttpServletRequest request) {
        String token = request.getHeader(AuthorizationInterceptor.LOGIN_TOKEN_KEY);
        if (StrUtil.isNotBlank(token)) {
            tokenService.removeToken(token);
        }
        request.getSession().invalidate();
        return R.ok("退出成功");
    }

    /**
     * 获取当前登录用户的 session 信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request) {
        Long id = (Long) request.getSession().getAttribute("user_id");
        HotelStaffEntity u = hotelStaffService.getById(id);
        return R.ok().put("data", u);
    }

    /**
     * 详情（个人资料用）
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        HotelStaffEntity u = hotelStaffService.getById(id);
        return R.ok().put("data", u);
    }

    /**
     * 修改（个人资料用；password 不在资料表单里，null 字段 MP 会跳过不动）
     */
    @RequestMapping("/update")
    public R update(@RequestBody HotelStaffEntity staff) {
        hotelStaffService.updateById(staff);
        return R.ok();
    }
}
