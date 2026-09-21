package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.user.entity.FriendEntity;
import com.zhiyouxing.user.entity.view.FriendView;
import com.zhiyouxing.user.entity.vo.FriendVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 好友表
 */
public interface FriendDao extends BaseMapper<FriendEntity> {
	
	List<FriendVO> selectListVO(@Param("ew") Wrapper<FriendEntity> wrapper);
	
	FriendVO selectVO(@Param("ew") Wrapper<FriendEntity> wrapper);
	
	List<FriendView> selectListView(@Param("ew") Wrapper<FriendEntity> wrapper);

	List<FriendView> selectListView(Page<?> page,@Param("ew") Wrapper<FriendEntity> wrapper);

    List<FriendView> selectFriendListView(Page<?> page, Map<String, Object> params);
	
	FriendView selectView(@Param("ew") Wrapper<FriendEntity> wrapper);
	

}
