package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.NewsTypeDao;
import com.zhiyouxing.travel.entity.NewsTypeEntity;
import com.zhiyouxing.travel.entity.view.NewsTypeView;
import com.zhiyouxing.travel.entity.vo.NewsTypeVO;
import com.zhiyouxing.travel.service.NewsTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("newsTypeService")
public class NewsTypeServiceImpl extends ServiceImpl<NewsTypeDao, NewsTypeEntity> implements NewsTypeService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<NewsTypeEntity> page = this.page(
                new Query<NewsTypeEntity>(params).getPage(),
                new QueryWrapper<NewsTypeEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<NewsTypeEntity> wrapper) {
		  Page<NewsTypeView> page =new Query<NewsTypeView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
    @Override
	public List<NewsTypeVO> selectListVO(Wrapper<NewsTypeEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public NewsTypeVO selectVO(Wrapper<NewsTypeEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<NewsTypeView> selectListView(Wrapper<NewsTypeEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public NewsTypeView selectView(Wrapper<NewsTypeEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
