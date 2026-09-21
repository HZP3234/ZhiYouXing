package com.zhiyouxing.attraction.service.impl;

import com.zhiyouxing.attraction.dao.AttractionTypeDao;
import com.zhiyouxing.attraction.entity.AttractionTypeEntity;
import com.zhiyouxing.attraction.entity.view.AttractionTypeView;
import com.zhiyouxing.attraction.entity.vo.AttractionTypeVO;
import com.zhiyouxing.attraction.service.AttractionTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("attractionTypeService")
public class AttractionTypeServiceImpl extends ServiceImpl<AttractionTypeDao, AttractionTypeEntity> implements AttractionTypeService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<AttractionTypeEntity> page = this.page(
                new Query<AttractionTypeEntity>(params).getPage(),
                new QueryWrapper<AttractionTypeEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<AttractionTypeEntity> wrapper) {
		  Page<AttractionTypeView> page =new Query<AttractionTypeView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<AttractionTypeVO> selectListVO(Wrapper<AttractionTypeEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public AttractionTypeVO selectVO(Wrapper<AttractionTypeEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<AttractionTypeView> selectListView(Wrapper<AttractionTypeEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public AttractionTypeView selectView(Wrapper<AttractionTypeEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
