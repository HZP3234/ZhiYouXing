package com.zhiyouxing.food.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zhiyouxing.food.dao.RestaurantStaffDao;
import com.zhiyouxing.food.entity.RestaurantStaffEntity;
import com.zhiyouxing.food.service.RestaurantStaffService;
import org.springframework.stereotype.Service;

@Service("restaurantStaffService")
public class RestaurantStaffServiceImpl extends ServiceImpl<RestaurantStaffDao, RestaurantStaffEntity>
		implements RestaurantStaffService {
}
