package com.zhiyouxing.attraction.service.impl;

import com.zhiyouxing.attraction.dao.AttractionCommentDao;
import com.zhiyouxing.attraction.entity.AttractionCommentEntity;
import com.zhiyouxing.attraction.entity.view.AttractionCommentView;
import com.zhiyouxing.attraction.entity.vo.AttractionCommentVO;
import com.zhiyouxing.attraction.service.AttractionCommentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("attractionCommentService")
public class AttractionCommentServiceImpl extends ServiceImpl<AttractionCommentDao, AttractionCommentEntity> implements AttractionCommentService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<AttractionCommentEntity> page = this.page(
                new Query<AttractionCommentEntity>(params).getPage(),
                new QueryWrapper<AttractionCommentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<AttractionCommentEntity> wrapper) {
		  Page<AttractionCommentView> page =new Query<AttractionCommentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<AttractionCommentVO> selectListVO(Wrapper<AttractionCommentEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public AttractionCommentVO selectVO(Wrapper<AttractionCommentEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<AttractionCommentView> selectListView(Wrapper<AttractionCommentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public AttractionCommentView selectView(Wrapper<AttractionCommentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
