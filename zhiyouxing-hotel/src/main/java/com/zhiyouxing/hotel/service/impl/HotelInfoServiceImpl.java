package com.zhiyouxing.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.hotel.dao.HotelInfoDao;
import com.zhiyouxing.hotel.entity.HotelInfoEntity;
import com.zhiyouxing.hotel.entity.view.HotelInfoView;
import com.zhiyouxing.hotel.entity.vo.HotelInfoVO;
import com.zhiyouxing.hotel.service.HotelInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("hotelInfoService")
public class HotelInfoServiceImpl extends ServiceImpl<HotelInfoDao, HotelInfoEntity> implements HotelInfoService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<HotelInfoEntity> page = this.page(
                new Query<HotelInfoEntity>(params).getPage(),
                new QueryWrapper<HotelInfoEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<HotelInfoEntity> wrapper) {
		  Page<HotelInfoView> page =new Query<HotelInfoView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<HotelInfoVO> selectListVO(Wrapper<HotelInfoEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public HotelInfoVO selectVO(Wrapper<HotelInfoEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<HotelInfoView> selectListView(Wrapper<HotelInfoEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public HotelInfoView selectView(Wrapper<HotelInfoEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
