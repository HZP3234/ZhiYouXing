package com.zhiyouxing.travel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.interceptor.AuthorizationInterceptor;
import com.zhiyouxing.common.service.TokenService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.travel.entity.TourGuideEntity;
import com.zhiyouxing.travel.entity.view.TourGuideView;
import com.zhiyouxing.travel.service.TourGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 导游
 */
@RestController
@RequestMapping("/tour_guide")
public class TourGuideController {
    @Autowired
    private TourGuideService tourGuideService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String captcha, HttpServletRequest request) {
        TourGuideEntity u = tourGuideService.getOne(new QueryWrapper<TourGuideEntity>().eq("guide_no", username));
        if(u!=null && u.getStatus().intValue()==1) {
            return R.error("账号已锁定，请联系管理员。");
        }
        if(u==null || !u.getPassword().equals(password)) {
            if(u!=null) {
                u.setPasswordWrongNum(u.getPasswordWrongNum()+1);
                if(u.getPasswordWrongNum()>=3) {
                    u.setStatus(1);
                }
                tourGuideService.updateById(u);
            }
            return R.error("账号或密码不正确");
        }

        String token = tokenService.generateToken(u.getId(), username,"tour_guide",  "导游" );
        return R.ok().put("token", token);
    }

    /**
     * 注册
     */
    @IgnoreAuth
    @RequestMapping("/register")
    public R register(@RequestBody TourGuideEntity tourGuide){
        TourGuideEntity u = tourGuideService.getOne(new QueryWrapper<TourGuideEntity>().eq("guide_no", tourGuide.getGuideNo()));
        if(u!=null) {
            return R.error("注册用户已存在");
        }
        /*
         * 同 UserController.register：前台注册不采集姓名，但 guide_name 是 NOT NULL，
         * 留空会直接 INSERT 失败，给个占位名。
         */
        if(StrUtil.isBlank(tourGuide.getGuideName())) {
            tourGuide.setGuideName("导游" + tourGuide.getGuideNo());
        }
        Long uId = new Date().getTime();
        tourGuide.setId(uId);
        tourGuideService.save(tourGuide);
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
        TourGuideEntity u = tourGuideService.getById(id);
        return R.ok().put("data", u);
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
        TourGuideEntity u = tourGuideService.getOne(new QueryWrapper<TourGuideEntity>().eq("guide_no", username));
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
        tourGuideService.updateById(u);
        return R.ok("密码修改成功");
    }

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TourGuideEntity tourGuide,
        HttpServletRequest request){
        QueryWrapper<TourGuideEntity> ew = new QueryWrapper<TourGuideEntity>();

        PageUtils page = tourGuideService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tourGuide), params), params));

        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TourGuideEntity tourGuide,
        HttpServletRequest request){
        QueryWrapper<TourGuideEntity> ew = new QueryWrapper<TourGuideEntity>();

        PageUtils page = tourGuideService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tourGuide), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TourGuideEntity tourGuide){
        QueryWrapper<TourGuideEntity> ew = new QueryWrapper<TourGuideEntity>();
        ew.allEq(MPUtil.allEQMapPre( tourGuide, "tour_guide"));
        return R.ok().put("data", tourGuideService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TourGuideEntity tourGuide){
        QueryWrapper< TourGuideEntity> ew = new QueryWrapper< TourGuideEntity>();
        ew.allEq(MPUtil.allEQMapPre( tourGuide, "tour_guide"));
        TourGuideView tourGuideView =  tourGuideService.selectView(ew);
        return R.ok("查询导游成功").put("data", tourGuideView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TourGuideEntity tourGuide = tourGuideService.getById(id);
        return R.ok().put("data", tourGuide);
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TourGuideEntity tourGuide = tourGuideService.getById(id);
        return R.ok().put("data", tourGuide);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TourGuideEntity tourGuide, HttpServletRequest request){
        if(tourGuideService.count(new QueryWrapper<TourGuideEntity>().eq("guide_no", tourGuide.getGuideNo()))>0) {
            return R.error("导游工号已存在");
        }
        tourGuide.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
        TourGuideEntity u = tourGuideService.getOne(new QueryWrapper<TourGuideEntity>().eq("guide_no", tourGuide.getGuideNo()));
        if(u!=null) {
            return R.error("用户已存在");
        }
        tourGuide.setId(new Date().getTime());
        tourGuideService.save(tourGuide);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TourGuideEntity tourGuide, HttpServletRequest request){
        if(tourGuideService.count(new QueryWrapper<TourGuideEntity>().eq("guide_no", tourGuide.getGuideNo()))>0) {
            return R.error("导游工号已存在");
        }
        tourGuide.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
        TourGuideEntity u = tourGuideService.getOne(new QueryWrapper<TourGuideEntity>().eq("guide_no", tourGuide.getGuideNo()));
        if(u!=null) {
            return R.error("用户已存在");
        }
        tourGuide.setId(new Date().getTime());
        tourGuideService.save(tourGuide);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TourGuideEntity tourGuide, HttpServletRequest request){
        if(tourGuideService.count(new QueryWrapper<TourGuideEntity>().ne("id", tourGuide.getId()).eq("guide_no", tourGuide.getGuideNo()))>0) {
            return R.error("导游工号已存在");
        }
        tourGuideService.updateById(tourGuide);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        tourGuideService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
}
