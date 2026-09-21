package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.user.entity.UsersEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户
 */
public interface UsersDao extends BaseMapper<UsersEntity> {
	
	List<UsersEntity> selectListView(@Param("ew") Wrapper<UsersEntity> wrapper);

	List<UsersEntity> selectListView(Page<?> page,@Param("ew") Wrapper<UsersEntity> wrapper);
	
}
