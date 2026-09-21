package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.TravelGuideDao;
import com.zhiyouxing.travel.entity.TravelGuideEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideView;
import com.zhiyouxing.travel.entity.vo.TravelGuideVO;
import com.zhiyouxing.travel.service.TravelGuideService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("travelGuideService")
public class TravelGuideServiceImpl extends ServiceImpl<TravelGuideDao, TravelGuideEntity> implements TravelGuideService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TravelGuideEntity> page = this.page(
                new Query<TravelGuideEntity>(params).getPage(),
                new QueryWrapper<TravelGuideEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TravelGuideEntity> wrapper) {
		  Page<TravelGuideView> page =new Query<TravelGuideView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<TravelGuideVO> selectListVO(Wrapper<TravelGuideEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public TravelGuideVO selectVO(Wrapper<TravelGuideEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<TravelGuideView> selectListView(Wrapper<TravelGuideEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TravelGuideView selectView(Wrapper<TravelGuideEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
