package com.zhiyouxing.common.interceptor;

import cn.hutool.json.JSONUtil;
import com.zhiyouxing.common.annotation.IgnoreAuth;
import com.zhiyouxing.common.entity.TokenEntity;
import com.zhiyouxing.common.service.TokenService;
import com.zhiyouxing.common.utils.R;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Set;

/**
 * 权限(Token)验证
 */
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    public static final String LOGIN_TOKEN_KEY = "Token";

    @Autowired
    private TokenService tokenService;
    
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		//支持跨域请求
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,request-source,Token, Origin,imgType, Content-Type, cache-control,postman-token,Cookie, Accept,authorization");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
	// 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
	if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
        	response.setStatus(HttpStatus.OK.value());
            return false;
        }
        
        IgnoreAuth annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(IgnoreAuth.class);
        } else {
            return true;
        }

        //从header中获取token
        String token = request.getHeader(LOGIN_TOKEN_KEY);
        
        /**
         * 不需要验证权限的方法直接放过
         */
        if(annotation!=null) {
        	return true;
        }
        
        TokenEntity tokenEntity = null;
        if(StrUtil.isNotBlank(token)) {
        	tokenEntity = tokenService.getTokenEntity(token);
        }
        
        if(tokenEntity != null) {
        	// 授权检查(#12)：/users 下除登录注册那几条外，只允许「管理员」角色访问
        	if (needsAdminRole(request.getRequestURI()) && !"管理员".equals(tokenEntity.getRole())) {
        		response.setCharacterEncoding("UTF-8");
        		response.setContentType("application/json; charset=utf-8");
        		PrintWriter out = response.getWriter();
        		out.print(JSONUtil.toJsonStr(R.error(403, "无权限访问")));
        		out.close();
        		return false;
        	}
        	request.getSession().setAttribute("user_id", tokenEntity.getUserId());
        	request.getSession().setAttribute("role", tokenEntity.getRole());
        	request.getSession().setAttribute("table_name", tokenEntity.getTableName());
        	request.getSession().setAttribute("username", tokenEntity.getUsername());
        	return true;
        }
        
		PrintWriter writer = null;
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		try {
		    writer = response.getWriter();
		    writer.print(JSONUtil.toJsonStr(R.error(401, "请先登录")));
		} finally {
		    if(writer != null){
		        writer.close();
		    }
		}
//				throw new EIException("请先登录", 401);
		return false;
    }

    /**
     * /users 下这几条是「谁都能调」的：登录注册、退出、改自己的密码、看自己的会话。
     * login/register 另由 @IgnoreAuth 放行（走不到这里），列出来是为了**白名单是完整的**：
     * 下面按「不在白名单就要管理员」判断，漏掉一条会连带把管理员自己锁在门外。
     */
    private static final Set<String> USERS_PUBLIC_ACTIONS =
            Set.of("login", "register", "logout", "resetPass", "session");

    /**
     * 请求是否落在「仅限管理员」的 /users 端点上。
     *
     * 原来只列了 page/list/save/update/delete/info 这几条，加了审核接口之后
     * （/users/user_identity/**、/users/qualification/**）那种写法就漏了 ——
     * 审核端点一条都不在名单里，等于任何登录用户都能改别人的认证结论。
     * 所以改成反向判断：/users 下除了 white list，其余一律要管理员。
     *
     * 取第一个路径段当动作名：/users/user_identity/page → user_identity。
     */
    private boolean needsAdminRole(String uri) {
        if (uri == null) {
            return false;
        }
        int idx = uri.indexOf("/users/");
        if (idx < 0) {
            return false;
        }
        String rest = uri.substring(idx + "/users/".length());
        int slash = rest.indexOf('/');
        String action = slash >= 0 ? rest.substring(0, slash) : rest;
        if (action.isEmpty()) {
            return false;
        }
        return !USERS_PUBLIC_ACTIONS.contains(action);
    }
}
