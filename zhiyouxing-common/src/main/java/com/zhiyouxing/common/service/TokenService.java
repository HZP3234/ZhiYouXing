
package com.zhiyouxing.common.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.entity.TokenEntity;
import com.zhiyouxing.common.utils.PageUtils;

import java.util.List;
import java.util.Map;


/**
 * token
 */
public interface TokenService extends IService<TokenEntity> {
 	PageUtils queryPage(Map<String, Object> params);
    
   	List<TokenEntity> selectListView(Wrapper<TokenEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TokenEntity> wrapper);
	
   	String generateToken(Long userId,String username,String tableName, String role);

   	TokenEntity getTokenEntity(String token);

   	/** 退出登录时删除 token */
   	void removeToken(String token);
}
