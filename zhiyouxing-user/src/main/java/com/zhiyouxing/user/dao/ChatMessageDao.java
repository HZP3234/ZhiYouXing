package com.zhiyouxing.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyouxing.user.entity.ChatMessageEntity;
import com.zhiyouxing.user.entity.view.ChatMessageView;
import com.zhiyouxing.user.entity.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息表
 */
public interface ChatMessageDao extends BaseMapper<ChatMessageEntity> {
	
	List<ChatMessageVO> selectListVO(@Param("ew") Wrapper<ChatMessageEntity> wrapper);
	
	ChatMessageVO selectVO(@Param("ew") Wrapper<ChatMessageEntity> wrapper);
	
	List<ChatMessageView> selectListView(@Param("ew") Wrapper<ChatMessageEntity> wrapper);

	List<ChatMessageView> selectListView(Page<?> page,@Param("ew") Wrapper<ChatMessageEntity> wrapper);

	
	ChatMessageView selectView(@Param("ew") Wrapper<ChatMessageEntity> wrapper);
	

}
