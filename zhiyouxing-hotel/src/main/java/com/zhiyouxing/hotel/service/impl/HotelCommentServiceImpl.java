package com.zhiyouxing.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.common.utils.PageUtils;
import com.zhiyouxing.common.utils.Query;
import com.zhiyouxing.hotel.dao.HotelCommentDao;
import com.zhiyouxing.hotel.entity.HotelCommentEntity;
import com.zhiyouxing.hotel.entity.view.HotelCommentView;
import com.zhiyouxing.hotel.entity.vo.HotelCommentVO;
import com.zhiyouxing.hotel.service.HotelCommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("hotelCommentService")
public class HotelCommentServiceImpl extends ServiceImpl<HotelCommentDao, HotelCommentEntity> implements HotelCommentService {
	
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<HotelCommentEntity> page = this.page(
                new Query<HotelCommentEntity>(params).getPage(),
                new QueryWrapper<HotelCommentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<HotelCommentEntity> wrapper) {
		  Page<HotelCommentView> page =new Query<HotelCommentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}

    
    @Override
	public List<HotelCommentVO> selectListVO(Wrapper<HotelCommentEntity> wrapper) {
 		return baseMapper.selectListVO(wrapper);
	}
	
	@Override
	public HotelCommentVO selectVO(Wrapper<HotelCommentEntity> wrapper) {
 		return baseMapper.selectVO(wrapper);
	}
	
	@Override
	public List<HotelCommentView> selectListView(Wrapper<HotelCommentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public HotelCommentView selectView(Wrapper<HotelCommentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
