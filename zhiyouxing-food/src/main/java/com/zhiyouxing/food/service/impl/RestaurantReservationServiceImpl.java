package com.zhiyouxing.food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.food.dao.RestaurantReservationDao;
import com.zhiyouxing.food.entity.RestaurantReservationEntity;
import com.zhiyouxing.food.entity.view.RestaurantReservationView;
import com.zhiyouxing.food.entity.vo.RestaurantReservationVO;
import com.zhiyouxing.food.service.RestaurantReservationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("restaurantReservationService")
public class RestaurantReservationServiceImpl extends ServiceImpl<RestaurantReservationDao, RestaurantReservationEntity> implements RestaurantReservationService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<RestaurantReservationEntity> page = this.page(
                new Query<RestaurantReservationEntity>(params).getPage(),
                new QueryWrapper<RestaurantReservationEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<RestaurantReservationEntity> wrapper) {
		  Page<RestaurantReservationView> page =new Query<RestaurantReservationView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<RestaurantReservationVO> selectListVO(Wrapper<RestaurantReservationEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public RestaurantReservationVO selectVO(Wrapper<RestaurantReservationEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<RestaurantReservationView> selectListView(Wrapper<RestaurantReservationEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public RestaurantReservationView selectView(Wrapper<RestaurantReservationEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<RestaurantReservationEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<RestaurantReservationEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<RestaurantReservationEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }




}
