package com.zhiyouxing.travel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.travel.entity.FriendLinkEntity;
import com.zhiyouxing.travel.entity.view.FriendLinkView;
import com.zhiyouxing.travel.entity.vo.FriendLinkVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 友情链接
 */
public interface FriendLinkDao extends BaseMapper<FriendLinkEntity> {
	
	List<FriendLinkVO> selectListVO(@Param("ew") Wrapper<FriendLinkEntity> wrapper);
	
	FriendLinkVO selectVO(@Param("ew") Wrapper<FriendLinkEntity> wrapper);
	
	List<FriendLinkView> selectListView(@Param("ew") Wrapper<FriendLinkEntity> wrapper);

	List<FriendLinkView> selectListView(Page<?> page,@Param("ew") Wrapper<FriendLinkEntity> wrapper);

	
	FriendLinkView selectView(@Param("ew") Wrapper<FriendLinkEntity> wrapper);
	

}
