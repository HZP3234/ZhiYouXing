
package com.zhiyouxing.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.user.entity.UsersEntity;

import java.util.List;
import java.util.Map;


/**
 * 系统用户
 */
public interface UsersService extends IService<UsersEntity> {
 	PageUtils queryPage(Map<String, Object> params);
    
   	List<UsersEntity> selectListView(Wrapper<UsersEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<UsersEntity> wrapper);
	   	
}
