package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.user.entity.UserEntity;
import com.zhiyouxing.user.entity.view.UserView;
import com.zhiyouxing.user.entity.vo.UserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户
 */
public interface UserDao extends BaseMapper<UserEntity> {
	
	List<UserVO> selectListVO(@Param("ew") Wrapper<UserEntity> wrapper);
	
	UserVO selectVO(@Param("ew") Wrapper<UserEntity> wrapper);
	
	List<UserView> selectListView(@Param("ew") Wrapper<UserEntity> wrapper);

	List<UserView> selectListView(Page<?> page,@Param("ew") Wrapper<UserEntity> wrapper);

	
	UserView selectView(@Param("ew") Wrapper<UserEntity> wrapper);
	

}
