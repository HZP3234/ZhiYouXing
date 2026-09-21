package com.zhiyouxing.hotel.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.hotel.dao.HotelStaffDao;
import com.zhiyouxing.hotel.entity.HotelStaffEntity;
import com.zhiyouxing.hotel.service.HotelStaffService;
import org.springframework.stereotype.Service;

@Service("hotelStaffService")
public class HotelStaffServiceImpl extends ServiceImpl<HotelStaffDao, HotelStaffEntity>
		implements HotelStaffService {
}
