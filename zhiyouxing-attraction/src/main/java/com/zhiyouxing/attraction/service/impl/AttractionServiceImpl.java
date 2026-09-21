package com.zhiyouxing.attraction.service.impl;

import com.zhiyouxing.attraction.dao.AttractionDao;
import com.zhiyouxing.attraction.entity.AttractionEntity;
import com.zhiyouxing.attraction.entity.view.AttractionView;
import com.zhiyouxing.attraction.entity.vo.AttractionVO;
import com.zhiyouxing.attraction.service.AttractionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("attractionService")
public class AttractionServiceImpl extends ServiceImpl<AttractionDao, AttractionEntity> implements AttractionService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<AttractionEntity> page = this.page(
                new Query<AttractionEntity>(params).getPage(),
                new QueryWrapper<AttractionEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<AttractionEntity> wrapper) {
		  Page<AttractionView> page =new Query<AttractionView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<AttractionVO> selectListVO(Wrapper<AttractionEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public AttractionVO selectVO(Wrapper<AttractionEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<AttractionView> selectListView(Wrapper<AttractionEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public AttractionView selectView(Wrapper<AttractionEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
