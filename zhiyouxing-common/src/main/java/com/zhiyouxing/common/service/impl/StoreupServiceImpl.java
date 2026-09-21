package com.zhiyouxing.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.dao.StoreupDao;
import com.zhiyouxing.common.entity.StoreupEntity;
import com.zhiyouxing.common.entity.view.StoreupView;
import com.zhiyouxing.common.entity.vo.StoreupVO;
import com.zhiyouxing.common.service.StoreupService;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("storeUpService")
public class StoreupServiceImpl extends ServiceImpl<StoreupDao, StoreupEntity> implements StoreupService {


	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		Page<StoreupEntity> page = this.page(
				new Query<StoreupEntity>(params).getPage(),
				new QueryWrapper<StoreupEntity>()
		);
		return new PageUtils(page);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<StoreupEntity> wrapper) {
		Page<StoreupView> page = new Query<StoreupView>(params).getPage();
		page.setRecords(baseMapper.selectListView(page, wrapper));
		PageUtils pageUtil = new PageUtils(page);
		return pageUtil;
	}

	@Override
	public List<StoreupVO> selectListVO(Wrapper<StoreupEntity> wrapper) {
		return baseMapper.selectListVO(wrapper);
	}

	@Override
	public StoreupVO selectVO(Wrapper<StoreupEntity> wrapper) {
		return baseMapper.selectVO(wrapper);
	}

	@Override
	public List<StoreupView> selectListView(Wrapper<StoreupEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public StoreupView selectView(Wrapper<StoreupEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}
