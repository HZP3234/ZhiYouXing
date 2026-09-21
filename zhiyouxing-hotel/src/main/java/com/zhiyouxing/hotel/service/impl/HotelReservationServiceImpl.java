package com.zhiyouxing.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.hotel.dao.HotelReservationDao;
import com.zhiyouxing.hotel.entity.HotelReservationEntity;
import com.zhiyouxing.hotel.entity.view.HotelReservationView;
import com.zhiyouxing.hotel.entity.vo.HotelReservationVO;
import com.zhiyouxing.hotel.service.HotelReservationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("hotelReservationService")
public class HotelReservationServiceImpl extends ServiceImpl<HotelReservationDao, HotelReservationEntity> implements HotelReservationService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<HotelReservationEntity> page = this.page(
                new Query<HotelReservationEntity>(params).getPage(),
                new QueryWrapper<HotelReservationEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<HotelReservationEntity> wrapper) {
		  Page<HotelReservationView> page =new Query<HotelReservationView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<HotelReservationVO> selectListVO(Wrapper<HotelReservationEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public HotelReservationVO selectVO(Wrapper<HotelReservationEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<HotelReservationView> selectListView(Wrapper<HotelReservationEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public HotelReservationView selectView(Wrapper<HotelReservationEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<HotelReservationEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<HotelReservationEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<HotelReservationEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }




}
