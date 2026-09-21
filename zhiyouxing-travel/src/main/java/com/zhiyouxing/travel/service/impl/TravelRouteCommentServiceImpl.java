package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.TravelRouteCommentDao;
import com.zhiyouxing.travel.entity.TravelRouteCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteCommentView;
import com.zhiyouxing.travel.entity.vo.TravelRouteCommentVO;
import com.zhiyouxing.travel.service.TravelRouteCommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("travelRouteCommentService")
public class TravelRouteCommentServiceImpl extends ServiceImpl<TravelRouteCommentDao, TravelRouteCommentEntity> implements TravelRouteCommentService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TravelRouteCommentEntity> page = this.page(
                new Query<TravelRouteCommentEntity>(params).getPage(),
                new QueryWrapper<TravelRouteCommentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TravelRouteCommentEntity> wrapper) {
		  Page<TravelRouteCommentView> page =new Query<TravelRouteCommentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<TravelRouteCommentVO> selectListVO(Wrapper<TravelRouteCommentEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public TravelRouteCommentVO selectVO(Wrapper<TravelRouteCommentEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<TravelRouteCommentView> selectListView(Wrapper<TravelRouteCommentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TravelRouteCommentView selectView(Wrapper<TravelRouteCommentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
