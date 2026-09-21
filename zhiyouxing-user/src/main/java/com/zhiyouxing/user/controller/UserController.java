package com.zhiyouxing.user.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.interceptor.AuthorizationInterceptor;
import com.zhiyouxing.common.service.TokenService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.entity.UserEntity;
import com.zhiyouxing.user.entity.view.UserView;
import com.zhiyouxing.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 用户
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String captcha, HttpServletRequest request) {
        UserEntity u = userService.getOne(new QueryWrapper<UserEntity>().eq("user_account", username));
        if(u!=null && u.getStatus().intValue()==1) {
            return R.error("账号已锁定，请联系管理员。");
        }
        if(u==null || !u.getPassword().equals(password)) {
            if(u!=null) {
                u.setPasswordWrongNum(u.getPasswordWrongNum()+1);
                if(u.getPasswordWrongNum()>=3) {
                    u.setStatus(1);
                }
                userService.updateById(u);
            }
            return R.error("账号或密码不正确");
        }

        String token = tokenService.generateToken(u.getId(), username,"user",  "用户" );
        return R.ok().put("token", token);
    }

    /**
     * 注册
     */
    @IgnoreAuth
    @RequestMapping("/register")
    public R register(@RequestBody UserEntity user){
        UserEntity u = userService.getOne(new QueryWrapper<UserEntity>().eq("user_account", user.getUserAccount()));
        if(u!=null) {
            return R.error("注册用户已存在");
        }
        /*
         * 前台注册只收手机号+密码，姓名不采集；但库里的 user_name 是 NOT NULL，
         * 留空会直接 INSERT 失败。给个占位名（管理端新增用户仍可传真名，不覆盖）。
         */
        if(StrUtil.isBlank(user.getUserName())) {
            user.setUserName("游客" + user.getUserAccount());
        }
        Long uId = new Date().getTime();
        user.setId(uId);
        userService.save(user);
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
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
        Long id = (Long)request.getSession().getAttribute("user_id");
        UserEntity u = userService.getById(id);
        return R.ok().put("data", u);
    }

    /**
     * 个人中心：读取本人资料（id 取登录态，不认请求参数）
     */
    @RequestMapping("/profile")
    public R profile(HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("user_id");
        if (userId == null) {
            return R.error(401, "请先登录");
        }
        return R.ok().put("data", userService.getById(userId));
    }

    /**
     * 个人中心：保存本人资料。
     *
     * 只认 user 表里可改的四列（姓名/性别/联系方式/头像），
     * 主键从登录态取 —— 复用 /user/update 的话，请求体里的 id 说了算，
     * 且密码、status 会被一起写回去，等于把改别人账号的入口开给了所有人。
     */
    @RequestMapping("/profile/update")
    public R updateProfile(@RequestBody UserEntity form, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("user_id");
        if (userId == null) {
            return R.error(401, "请先登录");
        }
        if (StrUtil.isBlank(form.getUserName())) {
            return R.error("姓名不能为空");
        }
        UserEntity db = userService.getById(userId);
        if (db == null) {
            return R.error("账号不存在");
        }
        db.setUserName(form.getUserName().trim());
        db.setGender(form.getGender());
        db.setContactPhone(form.getContactPhone());
        db.setAvatar(form.getAvatar());
        userService.updateById(db);
        return R.ok();
    }

    /**
     * 密码重置
     */
    @RequestMapping(value = "/resetPass")
    public R resetPass(String username, String oldPassword, String newPassword, HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user_id");
        if (userId == null) {
            return R.error(401, "请先登录");
        }
        String currentUsername = (String) request.getSession().getAttribute("username");
        if (!username.equals(currentUsername)) {
            return R.error("只能重置本人密码");
        }
        UserEntity u = userService.getOne(new QueryWrapper<UserEntity>().eq("user_account", username));
        if(u==null) {
            return R.error("账号不存在");
        }
        if(oldPassword == null || !u.getPassword().equals(oldPassword)) {
            return R.error("原密码不正确");
        }
        if(newPassword == null || newPassword.length() < 6) {
            return R.error("新密码长度不能少于6位");
        }
        u.setPassword(newPassword);
        userService.updateById(u);
        return R.ok("密码修改成功");
    }

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,UserEntity user,
        HttpServletRequest request){
        QueryWrapper<UserEntity> ew = new QueryWrapper<UserEntity>();

        PageUtils page = userService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, user), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,UserEntity user,
        HttpServletRequest request){
        QueryWrapper<UserEntity> ew = new QueryWrapper<UserEntity>();

        PageUtils page = userService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, user), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( UserEntity user){
        QueryWrapper<UserEntity> ew = new QueryWrapper<UserEntity>();
        ew.allEq(MPUtil.allEQMapPre( user, "user"));
        return R.ok().put("data", userService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(UserEntity user){
        QueryWrapper< UserEntity> ew = new QueryWrapper< UserEntity>();
        ew.allEq(MPUtil.allEQMapPre( user, "user"));
        UserView userView =  userService.selectView(ew);
        return R.ok("查询用户成功").put("data", userView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        UserEntity user = userService.getById(id);
        return R.ok().put("data", user);
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        UserEntity user = userService.getById(id);
        return R.ok().put("data", user);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody UserEntity user, HttpServletRequest request){
        if(userService.count(new QueryWrapper<UserEntity>().eq("user_account", user.getUserAccount()))>0) {
            return R.error("用户账号已存在");
        }
        user.setId(new Date().getTime() + (long)(Math.random()*1000));
        UserEntity u = userService.getOne(new QueryWrapper<UserEntity>().eq("user_account", user.getUserAccount()));
        if(u!=null) {
            return R.error("用户已存在");
        }
        user.setId(new Date().getTime());
        userService.save(user);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody UserEntity user, HttpServletRequest request){
        if(userService.count(new QueryWrapper<UserEntity>().eq("user_account", user.getUserAccount()))>0) {
            return R.error("用户账号已存在");
        }
        user.setId(new Date().getTime() + (long)(Math.random()*1000));
        UserEntity u = userService.getOne(new QueryWrapper<UserEntity>().eq("user_account", user.getUserAccount()));
        if(u!=null) {
            return R.error("用户已存在");
        }
        user.setId(new Date().getTime());
        userService.save(user);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody UserEntity user, HttpServletRequest request){
        if(userService.count(new QueryWrapper<UserEntity>().ne("id", user.getId()).eq("user_account", user.getUserAccount()))>0) {
            return R.error("用户账号已存在");
        }
        userService.updateById(user);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        userService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
}
