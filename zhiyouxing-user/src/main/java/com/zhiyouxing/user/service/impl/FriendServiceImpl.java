package com.zhiyouxing.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.user.dao.FriendDao;
import com.zhiyouxing.user.entity.FriendEntity;
import com.zhiyouxing.user.entity.view.FriendView;
import com.zhiyouxing.user.entity.vo.FriendVO;
import com.zhiyouxing.user.service.FriendService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("friendService")
public class FriendServiceImpl extends ServiceImpl<FriendDao, FriendEntity> implements FriendService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<FriendEntity> page = this.page(
                new Query<FriendEntity>(params).getPage(),
                new QueryWrapper<FriendEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<FriendEntity> wrapper) {
		  Page<FriendView> page =new Query<FriendView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
    public PageUtils queryFriendPage(Map<String, Object> params) {
        Page<FriendView> page =new Query<FriendView>(params).getPage();
        page.setRecords(baseMapper.selectFriendListView(page, params));
        PageUtils pageUtil = new PageUtils(page);
        return pageUtil;
    }
    
    @Override
	public List<FriendVO> selectListVO(Wrapper<FriendEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public FriendVO selectVO(Wrapper<FriendEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<FriendView> selectListView(Wrapper<FriendEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public FriendView selectView(Wrapper<FriendEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
