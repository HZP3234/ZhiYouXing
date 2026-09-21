package com.zhiyouxing.food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.food.dao.RestaurantDao;
import com.zhiyouxing.food.entity.RestaurantEntity;
import com.zhiyouxing.food.entity.view.RestaurantView;
import com.zhiyouxing.food.entity.vo.RestaurantVO;
import com.zhiyouxing.food.service.RestaurantService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("restaurantService")
public class RestaurantServiceImpl extends ServiceImpl<RestaurantDao, RestaurantEntity> implements RestaurantService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<RestaurantEntity> page = this.page(
                new Query<RestaurantEntity>(params).getPage(),
                new QueryWrapper<RestaurantEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<RestaurantEntity> wrapper) {
		  Page<RestaurantView> page =new Query<RestaurantView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<RestaurantVO> selectListVO(Wrapper<RestaurantEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public RestaurantVO selectVO(Wrapper<RestaurantEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<RestaurantView> selectListView(Wrapper<RestaurantEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public RestaurantView selectView(Wrapper<RestaurantEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
