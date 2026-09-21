package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.TravelGuideCommentDao;
import com.zhiyouxing.travel.entity.TravelGuideCommentEntity;
import com.zhiyouxing.travel.entity.view.TravelGuideCommentView;
import com.zhiyouxing.travel.entity.vo.TravelGuideCommentVO;
import com.zhiyouxing.travel.service.TravelGuideCommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("travelGuideCommentService")
public class TravelGuideCommentServiceImpl extends ServiceImpl<TravelGuideCommentDao, TravelGuideCommentEntity> implements TravelGuideCommentService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TravelGuideCommentEntity> page = this.page(
                new Query<TravelGuideCommentEntity>(params).getPage(),
                new QueryWrapper<TravelGuideCommentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TravelGuideCommentEntity> wrapper) {
		  Page<TravelGuideCommentView> page =new Query<TravelGuideCommentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
    @Override
	public List<TravelGuideCommentVO> selectListVO(Wrapper<TravelGuideCommentEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public TravelGuideCommentVO selectVO(Wrapper<TravelGuideCommentEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<TravelGuideCommentView> selectListView(Wrapper<TravelGuideCommentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TravelGuideCommentView selectView(Wrapper<TravelGuideCommentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
