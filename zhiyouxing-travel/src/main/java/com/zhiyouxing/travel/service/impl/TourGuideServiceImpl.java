package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.TourGuideDao;
import com.zhiyouxing.travel.entity.TourGuideEntity;
import com.zhiyouxing.travel.entity.view.TourGuideView;
import com.zhiyouxing.travel.entity.vo.TourGuideVO;
import com.zhiyouxing.travel.service.TourGuideService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("tourGuideService")
public class TourGuideServiceImpl extends ServiceImpl<TourGuideDao, TourGuideEntity> implements TourGuideService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TourGuideEntity> page = this.page(
                new Query<TourGuideEntity>(params).getPage(),
                new QueryWrapper<TourGuideEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TourGuideEntity> wrapper) {
		  Page<TourGuideView> page =new Query<TourGuideView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<TourGuideVO> selectListVO(Wrapper<TourGuideEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public TourGuideVO selectVO(Wrapper<TourGuideEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<TourGuideView> selectListView(Wrapper<TourGuideEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TourGuideView selectView(Wrapper<TourGuideEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
