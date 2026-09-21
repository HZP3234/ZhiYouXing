package com.zhiyouxing.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.user.entity.FriendEntity;
import com.zhiyouxing.user.entity.view.FriendView;
import com.zhiyouxing.user.entity.vo.FriendVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 好友表
 */
public interface FriendService extends IService<FriendEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<FriendVO> selectListVO(Wrapper<FriendEntity> wrapper);
   	
   	FriendVO selectVO(@Param("ew") Wrapper<FriendEntity> wrapper);
   	
   	List<FriendView> selectListView(Wrapper<FriendEntity> wrapper);
   	
   	FriendView selectView(@Param("ew") Wrapper<FriendEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<FriendEntity> wrapper);

    PageUtils queryFriendPage(Map<String, Object> params);
   	

}

