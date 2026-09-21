package com.zhiyouxing.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.travel.dao.GroupTourDao;
import com.zhiyouxing.travel.entity.GroupTourEntity;
import com.zhiyouxing.travel.entity.view.GroupTourView;
import com.zhiyouxing.travel.entity.vo.GroupTourVO;
import com.zhiyouxing.travel.service.GroupTourService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("groupTourService")
public class GroupTourServiceImpl extends ServiceImpl<GroupTourDao, GroupTourEntity> implements GroupTourService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<GroupTourEntity> page = this.page(
                new Query<GroupTourEntity>(params).getPage(),
                new QueryWrapper<GroupTourEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<GroupTourEntity> wrapper) {
		  Page<GroupTourView> page =new Query<GroupTourView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    @Override
	public List<GroupTourVO> selectListVO(Wrapper<GroupTourEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public GroupTourVO selectVO(Wrapper<GroupTourEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<GroupTourView> selectListView(Wrapper<GroupTourEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public GroupTourView selectView(Wrapper<GroupTourEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<GroupTourEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<GroupTourEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<GroupTourEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }

}
