package com.zhiyouxing.attraction.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.attraction.dao.AttractionStaffDao;
import com.zhiyouxing.attraction.entity.AttractionStaffEntity;
import com.zhiyouxing.attraction.service.AttractionStaffService;
import org.springframework.stereotype.Service;

@Service("attractionStaffService")
public class AttractionStaffServiceImpl extends ServiceImpl<AttractionStaffDao, AttractionStaffEntity>
		implements AttractionStaffService {
}
