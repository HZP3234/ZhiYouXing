package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.TravelRouteDao;
import com.zhiyouxing.travel.entity.TravelRouteEntity;
import com.zhiyouxing.travel.entity.view.TravelRouteView;
import com.zhiyouxing.travel.entity.vo.TravelRouteVO;
import com.zhiyouxing.travel.service.TravelRouteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("travelRouteService")
public class TravelRouteServiceImpl extends ServiceImpl<TravelRouteDao, TravelRouteEntity> implements TravelRouteService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TravelRouteEntity> page = this.page(
                new Query<TravelRouteEntity>(params).getPage(),
                new QueryWrapper<TravelRouteEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TravelRouteEntity> wrapper) {
		  Page<TravelRouteView> page =new Query<TravelRouteView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<TravelRouteVO> selectListVO(Wrapper<TravelRouteEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public TravelRouteVO selectVO(Wrapper<TravelRouteEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<TravelRouteView> selectListView(Wrapper<TravelRouteEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TravelRouteView selectView(Wrapper<TravelRouteEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
