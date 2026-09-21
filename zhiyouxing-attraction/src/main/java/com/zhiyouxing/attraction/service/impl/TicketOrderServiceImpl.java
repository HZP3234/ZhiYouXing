package com.zhiyouxing.attraction.service.impl;

import com.zhiyouxing.attraction.dao.TicketOrderDao;
import com.zhiyouxing.attraction.entity.TicketOrderEntity;
import com.zhiyouxing.attraction.entity.view.TicketOrderView;
import com.zhiyouxing.attraction.entity.vo.TicketOrderVO;
import com.zhiyouxing.attraction.service.TicketOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("ticketOrderService")
public class TicketOrderServiceImpl extends ServiceImpl<TicketOrderDao, TicketOrderEntity> implements TicketOrderService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TicketOrderEntity> page = this.page(
                new Query<TicketOrderEntity>(params).getPage(),
                new QueryWrapper<TicketOrderEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TicketOrderEntity> wrapper) {
		  Page<TicketOrderView> page =new Query<TicketOrderView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<TicketOrderVO> selectListVO(Wrapper<TicketOrderEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public TicketOrderVO selectVO(Wrapper<TicketOrderEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<TicketOrderView> selectListView(Wrapper<TicketOrderEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TicketOrderView selectView(Wrapper<TicketOrderEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<TicketOrderEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<TicketOrderEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<TicketOrderEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }




}
