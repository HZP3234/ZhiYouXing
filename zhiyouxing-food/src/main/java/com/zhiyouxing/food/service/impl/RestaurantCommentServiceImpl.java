package com.zhiyouxing.food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.food.dao.RestaurantCommentDao;
import com.zhiyouxing.food.entity.RestaurantCommentEntity;
import com.zhiyouxing.food.entity.view.RestaurantCommentView;
import com.zhiyouxing.food.entity.vo.RestaurantCommentVO;
import com.zhiyouxing.food.service.RestaurantCommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("restaurantCommentService")
public class RestaurantCommentServiceImpl extends ServiceImpl<RestaurantCommentDao, RestaurantCommentEntity> implements RestaurantCommentService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<RestaurantCommentEntity> page = this.page(
                new Query<RestaurantCommentEntity>(params).getPage(),
                new QueryWrapper<RestaurantCommentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<RestaurantCommentEntity> wrapper) {
		  Page<RestaurantCommentView> page =new Query<RestaurantCommentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<RestaurantCommentVO> selectListVO(Wrapper<RestaurantCommentEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public RestaurantCommentVO selectVO(Wrapper<RestaurantCommentEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<RestaurantCommentView> selectListView(Wrapper<RestaurantCommentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public RestaurantCommentView selectView(Wrapper<RestaurantCommentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
