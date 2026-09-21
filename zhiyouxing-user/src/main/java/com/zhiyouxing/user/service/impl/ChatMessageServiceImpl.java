package com.zhiyouxing.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.user.dao.ChatMessageDao;
import com.zhiyouxing.user.entity.ChatMessageEntity;
import com.zhiyouxing.user.entity.view.ChatMessageView;
import com.zhiyouxing.user.entity.vo.ChatMessageVO;
import com.zhiyouxing.user.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("chatMessageService")
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageDao, ChatMessageEntity> implements ChatMessageService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ChatMessageEntity> page = this.page(
                new Query<ChatMessageEntity>(params).getPage(),
                new QueryWrapper<ChatMessageEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<ChatMessageEntity> wrapper) {
		  Page<ChatMessageView> page =new Query<ChatMessageView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<ChatMessageVO> selectListVO(Wrapper<ChatMessageEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public ChatMessageVO selectVO(Wrapper<ChatMessageEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<ChatMessageView> selectListView(Wrapper<ChatMessageEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public ChatMessageView selectView(Wrapper<ChatMessageEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
