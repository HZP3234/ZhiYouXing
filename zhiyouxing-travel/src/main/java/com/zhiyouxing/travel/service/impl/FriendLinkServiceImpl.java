package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.FriendLinkDao;
import com.zhiyouxing.travel.entity.FriendLinkEntity;
import com.zhiyouxing.travel.entity.view.FriendLinkView;
import com.zhiyouxing.travel.entity.vo.FriendLinkVO;
import com.zhiyouxing.travel.service.FriendLinkService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("friendLinkService")
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkDao, FriendLinkEntity> implements FriendLinkService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<FriendLinkEntity> page = this.page(
                new Query<FriendLinkEntity>(params).getPage(),
                new QueryWrapper<FriendLinkEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<FriendLinkEntity> wrapper) {
		  Page<FriendLinkView> page =new Query<FriendLinkView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<FriendLinkVO> selectListVO(Wrapper<FriendLinkEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public FriendLinkVO selectVO(Wrapper<FriendLinkEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<FriendLinkView> selectListView(Wrapper<FriendLinkEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public FriendLinkView selectView(Wrapper<FriendLinkEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
