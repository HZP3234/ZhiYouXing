package com.zhiyouxing.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.hotel.dao.RoomTypeDao;
import com.zhiyouxing.hotel.entity.RoomTypeEntity;
import com.zhiyouxing.hotel.entity.view.RoomTypeView;
import com.zhiyouxing.hotel.entity.vo.RoomTypeVO;
import com.zhiyouxing.hotel.service.RoomTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("roomTypeService")
public class RoomTypeServiceImpl extends ServiceImpl<RoomTypeDao, RoomTypeEntity> implements RoomTypeService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<RoomTypeEntity> page = this.page(
                new Query<RoomTypeEntity>(params).getPage(),
                new QueryWrapper<RoomTypeEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<RoomTypeEntity> wrapper) {
		  Page<RoomTypeView> page =new Query<RoomTypeView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<RoomTypeVO> selectListVO(Wrapper<RoomTypeEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public RoomTypeVO selectVO(Wrapper<RoomTypeEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<RoomTypeView> selectListView(Wrapper<RoomTypeEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public RoomTypeView selectView(Wrapper<RoomTypeEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
