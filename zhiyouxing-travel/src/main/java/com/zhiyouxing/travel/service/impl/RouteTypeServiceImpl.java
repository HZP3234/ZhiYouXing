package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.RouteTypeDao;
import com.zhiyouxing.travel.entity.RouteTypeEntity;
import com.zhiyouxing.travel.entity.view.RouteTypeView;
import com.zhiyouxing.travel.entity.vo.RouteTypeVO;
import com.zhiyouxing.travel.service.RouteTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("routeTypeService")
public class RouteTypeServiceImpl extends ServiceImpl<RouteTypeDao, RouteTypeEntity> implements RouteTypeService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<RouteTypeEntity> page = this.page(
                new Query<RouteTypeEntity>(params).getPage(),
                new QueryWrapper<RouteTypeEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<RouteTypeEntity> wrapper) {
		  Page<RouteTypeView> page =new Query<RouteTypeView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<RouteTypeVO> selectListVO(Wrapper<RouteTypeEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public RouteTypeVO selectVO(Wrapper<RouteTypeEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<RouteTypeView> selectListView(Wrapper<RouteTypeEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public RouteTypeView selectView(Wrapper<RouteTypeEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
