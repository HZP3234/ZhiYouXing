package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.HelpRequestDao;
import com.zhiyouxing.travel.entity.HelpRequestEntity;
import com.zhiyouxing.travel.entity.view.HelpRequestView;
import com.zhiyouxing.travel.entity.vo.HelpRequestVO;
import com.zhiyouxing.travel.service.HelpRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("helpRequestService")
public class HelpRequestServiceImpl extends ServiceImpl<HelpRequestDao, HelpRequestEntity> implements HelpRequestService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<HelpRequestEntity> page = this.page(
                new Query<HelpRequestEntity>(params).getPage(),
                new QueryWrapper<HelpRequestEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<HelpRequestEntity> wrapper) {
		  Page<HelpRequestView> page =new Query<HelpRequestView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<HelpRequestVO> selectListVO(Wrapper<HelpRequestEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public HelpRequestVO selectVO(Wrapper<HelpRequestEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<HelpRequestView> selectListView(Wrapper<HelpRequestEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public HelpRequestView selectView(Wrapper<HelpRequestEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<HelpRequestEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<HelpRequestEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<HelpRequestEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }

}
