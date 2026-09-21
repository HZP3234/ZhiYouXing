package com.zhiyouxing.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.user.entity.ChatMessageEntity;
import com.zhiyouxing.user.entity.view.ChatMessageView;
import com.zhiyouxing.user.entity.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 消息表
 */
public interface ChatMessageService extends IService<ChatMessageEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<ChatMessageVO> selectListVO(Wrapper<ChatMessageEntity> wrapper);
   	
   	ChatMessageVO selectVO(@Param("ew") Wrapper<ChatMessageEntity> wrapper);
   	
   	List<ChatMessageView> selectListView(Wrapper<ChatMessageEntity> wrapper);
   	
   	ChatMessageView selectView(@Param("ew") Wrapper<ChatMessageEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<ChatMessageEntity> wrapper);

   	

}

