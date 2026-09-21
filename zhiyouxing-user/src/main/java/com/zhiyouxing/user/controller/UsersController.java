
package com.zhiyouxing.user.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.interceptor.AuthorizationInterceptor;
import com.zhiyouxing.common.service.TokenService;
import com.zhiyouxing.common.utils.MPUtil;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.R;
import com.zhiyouxing.user.entity.UsersEntity;
import com.zhiyouxing.user.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * 登录相关
 */
@RequestMapping("users")
@RestController
public class UsersController{
	
	@Autowired
	private UsersService userService;
	
	@Autowired
	private TokenService tokenService;

	/**
	 * 登录
	 */
	@IgnoreAuth
	@RequestMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		UsersEntity user = userService.getOne(new QueryWrapper<UsersEntity>().eq("username", username));
		if(user==null || !user.getPassword().equals(password)) {
			return R.error("账号或密码不正确");
		}
		String token = tokenService.generateToken(user.getId(),username, "users", user.getRole());
		return R.ok().put("token", token);
	}
	
	/**
	 * 注册
	 */
	@IgnoreAuth
	@PostMapping(value = "/register")
	public R register(@RequestBody UsersEntity user){
//    	ValidatorUtils.validateEntity(user);
    	if(userService.getOne(new QueryWrapper<UsersEntity>().eq("username", user.getUsername())) !=null) {
    		return R.error("用户已存在");
    	}
    	// 安全修复(#10)：公开注册强制为普通用户角色，禁止通过数据库默认值('管理员')创建管理员账户
    	user.setRole("用户");
        userService.save(user);
        return R.ok();
    }

	/**
	 * 退出
	 */
	@GetMapping(value = "logout")
	public R logout(HttpServletRequest request) {
		String token = request.getHeader(AuthorizationInterceptor.LOGIN_TOKEN_KEY);
		if (StrUtil.isNotBlank(token)) {
			tokenService.removeToken(token);
		}
		request.getSession().invalidate();
		return R.ok("退出成功");
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
    	UsersEntity user = userService.getOne(new QueryWrapper<UsersEntity>().eq("username", username));
    	if(user==null) {
    		return R.error("账号不存在");
    	}
    	if(oldPassword == null || !user.getPassword().equals(oldPassword)) {
    		return R.error("原密码不正确");
    	}
    	if(newPassword == null || newPassword.length() < 6) {
    		return R.error("新密码长度不能少于6位");
    	}
    	user.setPassword(newPassword);
        userService.updateById(user);
        return R.ok("密码修改成功");
    }
	
	/**
     * 列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,UsersEntity user){
        QueryWrapper<UsersEntity> ew = new QueryWrapper<UsersEntity>();
    	PageUtils page = userService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, user), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/list")
    public R list( UsersEntity user){
       	QueryWrapper<UsersEntity> ew = new QueryWrapper<UsersEntity>();
      	ew.allEq(MPUtil.allEQMapPre( user, "user")); 
        return R.ok().put("data", userService.selectListView(ew));
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") String id){
        UsersEntity user = userService.getById(id);
        return R.ok().put("data", user);
    }
    
    /**
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
    	Long id = (Long)request.getSession().getAttribute("user_id");
        UsersEntity user = userService.getById(id);
        return R.ok().put("data", user);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody UsersEntity user){
//    	ValidatorUtils.validateEntity(user);
    	if(userService.getOne(new QueryWrapper<UsersEntity>().eq("username", user.getUsername())) !=null) {
    		return R.error("用户已存在");
    	}
        userService.save(user);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody UsersEntity user){
//        ValidatorUtils.validateEntity(user);
    	UsersEntity u = userService.getOne(new QueryWrapper<UsersEntity>().eq("username", user.getUsername()));
    	if(u!=null && u.getId()!=user.getId() && u.getUsername().equals(user.getUsername())) {
    		return R.error("用户名已存在。");
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
