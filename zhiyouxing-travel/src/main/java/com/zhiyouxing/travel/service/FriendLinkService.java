package com.zhiyouxing.travel.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.travel.entity.FriendLinkEntity;
import com.zhiyouxing.travel.entity.view.FriendLinkView;
import com.zhiyouxing.travel.entity.vo.FriendLinkVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 友情链接
 */
public interface FriendLinkService extends IService<FriendLinkEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<FriendLinkVO> selectListVO(Wrapper<FriendLinkEntity> wrapper);
   	
   	FriendLinkVO selectVO(@Param("ew") Wrapper<FriendLinkEntity> wrapper);
   	
   	List<FriendLinkView> selectListView(Wrapper<FriendLinkEntity> wrapper);
   	
   	FriendLinkView selectView(@Param("ew") Wrapper<FriendLinkEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<FriendLinkEntity> wrapper);

}

