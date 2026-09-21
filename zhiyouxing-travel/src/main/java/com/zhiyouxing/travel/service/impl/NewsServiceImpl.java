package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.NewsDao;
import com.zhiyouxing.travel.entity.NewsEntity;
import com.zhiyouxing.travel.entity.view.NewsView;
import com.zhiyouxing.travel.entity.vo.NewsVO;
import com.zhiyouxing.travel.service.NewsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("newsService")
public class NewsServiceImpl extends ServiceImpl<NewsDao, NewsEntity> implements NewsService {
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<NewsEntity> page = this.page(
                new Query<NewsEntity>(params).getPage(),
                new QueryWrapper<NewsEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<NewsEntity> wrapper) {
		  Page<NewsView> page =new Query<NewsView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<NewsVO> selectListVO(Wrapper<NewsEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public NewsVO selectVO(Wrapper<NewsEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<NewsView> selectListView(Wrapper<NewsEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public NewsView selectView(Wrapper<NewsEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
